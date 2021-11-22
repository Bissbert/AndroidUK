package ch.bissbert.peakseek.data;

import com.orm.SugarRecord;
import com.orm.dsl.Column;

public class Language extends SugarRecord {
    public static final Language NONE = new Language("NONE");
    private String name;

    @Column(name = "shortName", unique = true, notNull = true)
    private String shortName;

    public Language(String name) {
        this.name = name;
    }

    public Language() {
    }

    public Long getId() {
        return super.getId();
    }

    public void setId(long id) {
        super.setId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;

        Language language = (Language) o;

        return getName() != null ? getName().equalsIgnoreCase(language.getName()) : language.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
