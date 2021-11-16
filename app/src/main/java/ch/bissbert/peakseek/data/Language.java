package ch.bissbert.peakseek.data;

public class Language {
    public static final Language NONE = new Language("NONE");
    private int id;
    private String name;

    public Language(String name) {
        this.name = name;
    }

    public Language() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
