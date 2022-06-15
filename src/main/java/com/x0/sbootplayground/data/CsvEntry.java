package com.x0.sbootplayground.data;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.Date;

@Entity
public class CsvEntry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String source;

    private String codeListCode;

    @Column(unique = true)
    private String code;

    private String displayValue;

    private String longDescription;

    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    private Date toDate;

    private Integer sortingPriority;

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCodeListCode() {
        return codeListCode;
    }

    public void setCodeListCode(String codeListCode) {
        this.codeListCode = codeListCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Integer getSortingPriority() {
        return sortingPriority;
    }

    public void setSortingPriority(Integer sortingPriority) {
        this.sortingPriority = sortingPriority;
    }
}
