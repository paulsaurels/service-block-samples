package demo.event;

import demo.account.Account;
import demo.account.AccountEvent;
import demo.account.AccountEventRepository;
import demo.account.AccountRepository;
import demo.domain.LambdaResponse;
import demo.function.AccountCommandService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EventService {

    final private Logger log = Logger.getLogger(EventService.class);
    final private AccountRepository accountRepository;
    final private AccountEventRepository accountEventRepository;
    final private AccountCommandService accountCommandService;

    public EventService(AccountRepository accountRepository,
                        AccountEventRepository accountEventRepository,
                        AccountCommandService accountCommandService) {
        this.accountRepository = accountRepository;
        this.accountEventRepository = accountEventRepository;
        this.accountCommandService = accountCommandService;
    }

    public Account apply(AccountEvent accountEvent) {
        Assert.notNull(accountEvent.getAccountId(),
                "Account event must contain a valid account id");

        // Get the account referenced by the event
        Account account = accountRepository.findOne(accountEvent.getAccountId());
        Assert.notNull(account, "An account for that ID does not exist");

        // Get a history of events for this account
        List<AccountEvent> events = accountEventRepository
                .findEventsByAccountId(accountEvent.getAccountId());

        // Sort the events reverse chronological
        events.sort(Comparator.comparing(AccountEvent::getCreatedAt).reversed());

        LambdaResponse<Account> result = null;

        // Route requests to serverless functions
        switch (accountEvent.getType()) {
            case ACCOUNT_ACTIVATED:
                result = accountCommandService.getActivateAccount()
                        .apply(withPayload(accountEvent, events, account));
                break;
            case ACCOUNT_SUSPENDED:
                result = accountCommandService.getSuspendAccount()
                        .apply(withPayload(accountEvent, events, account));
                break;
        }

        if (result.getException() != null) {
            throw new RuntimeException(result.getException().getMessage(),
                    result.getException());
        }

        Assert.notNull(result.getPayload(), "Lambda response payload must not be null");

        log.info(result.getPayload());

        // Add the event and save the new account status
        addEvent(accountEvent, account);

        account.setStatus(result.getPayload().getStatus());
        account = accountRepository.save(account);
        accountRepository.flush();

        return account;
    }

    private AccountEvent addEvent(AccountEvent accountEvent, Account account) {
        accountEvent = accountEventRepository.save(accountEvent);
        account.getEvents().add(accountEvent);
        return accountEvent;
    }

    private AccountEvent withPayload(AccountEvent event, List<AccountEvent> events, Account account) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("account", account);
        eventMap.put("events", events);
        event.setPayload(eventMap);
        return event;
    }
}
