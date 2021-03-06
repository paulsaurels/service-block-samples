package demo.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;

@Entity
public class Commit extends AbstractEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private CommitStatus status;

    @JsonIgnore
    @Transient
    private Project project;

    private Long projectId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<File> files;

    private String author;

    private Long commitDate;

    public Commit() {
        status = CommitStatus.PUSHED;
    }

    public Commit(Long id) {
        this.id = id;
    }

    public Commit(List<File> files) {
        this();
        this.files = files;
    }

    public Commit(List<File> files, String author, Long commitDate) {
        this();
        this.files = files;
        this.author = author;
        this.commitDate = commitDate;
    }

    @JsonProperty("commitId")
    public Long getIdentity() {
        return this.id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    public CommitStatus getStatus() {
        return status;
    }

    public void setStatus(CommitStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Long commitDate) {
        this.commitDate = commitDate;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "id=" + id +
                ", status=" + status +
                ", project=" + project +
                ", projectId=" + projectId +
                ", files=" + (files != null ? String.valueOf(files.hashCode()) : null) +
                '}';
    }
}
