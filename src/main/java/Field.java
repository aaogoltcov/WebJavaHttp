public class Field {
    final String id;
    final String name;
    final String description;

    public Field(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}