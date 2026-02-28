package testgroup.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Film {
    private int id;
    private String title;
    private int year;
    private String genre;
    private boolean watched;

    // + Getters and setters
}