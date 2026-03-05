package testgroup.model; 
 
import java.util.List; 
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column; 
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String mail;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password; 

    // Список уроков, принадлежащих этому пользователю
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons; 

    // Список слов, составляющих словарь этого пользователя
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Word> words; 
    

}