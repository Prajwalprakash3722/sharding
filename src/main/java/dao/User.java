package dao;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private LocalDate date;

    public Integer getIdasInt() {
        return Integer.parseInt(id);
    }

}