package models;

/**
 * Interface representing any object that can be uniquely identified by an ID.
 * This acts as a contract: any class implementing this must provide ID management.
 */
public interface Identifiable {
    int getId();
    void setId(int id);
}
