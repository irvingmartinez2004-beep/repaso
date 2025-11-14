package edu.espe.springlab.repository;

import edu.espe.springlab.domain.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository repo;

    private Student build(String fullName, String email, boolean active) {
        Student s = new Student();
        s.setFullName(fullName);
        s.setEmail(email);
        s.setBirthDate(LocalDate.of(2000, 1, 1));
        s.setActive(active);
        return s;
    }

    // -------------------------------------------------------
    // 1. SAVE + FIND BY EMAIL
    // -------------------------------------------------------
    @Test
    void shouldSaveStudent() {
        Student saved = repo.save(build("Juan Perez", "juan@test.com", true));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldSaveAndFindByEmail() {
        repo.save(build("Maria Lopez", "maria@test.com", true));

        var result = repo.findByEmail("maria@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Maria Lopez");
    }

    // -------------------------------------------------------
    // 2. EXISTENCIA POR EMAIL
    // -------------------------------------------------------
    @Test
    void shouldReturnTrueIfEmailExists() {
        repo.save(build("User A", "exists@test.com", true));

        assertThat(repo.existsByEmail("exists@test.com")).isTrue();
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() {
        assertThat(repo.existsByEmail("no@test.com")).isFalse();
    }

    // -------------------------------------------------------
    // 3. FIND BY ID
    // -------------------------------------------------------
    @Test
    void shouldFindById() {
        Student s = repo.save(build("Ana", "ana@test.com", true));

        var found = repo.findById(s.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    void shouldReturnEmptyIfNotFoundById() {
        var result = repo.findById(999L);
        assertThat(result).isNotPresent();
    }

    // -------------------------------------------------------
    // 4. FIND ALL
    // -------------------------------------------------------
    @Test
    void shouldFindAllStudents() {
        repo.save(build("A", "a@mail.com", true));
        repo.save(build("B", "b@mail.com", true));

        List<Student> list = repo.findAll();

        assertThat(list).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListIfNoStudents() {
        List<Student> list = repo.findAll();
        assertThat(list).isEmpty();
    }

    // -------------------------------------------------------
    // 5. DELETE BY ID
    // -------------------------------------------------------
    @Test
    void shouldDeleteById() {
        Student s = repo.save(build("Delete User", "delete@test.com", true));

        repo.deleteById(s.getId());

        assertThat(repo.findById(s.getId())).isNotPresent();
    }

    // -------------------------------------------------------
    // 6. UNIQUE EMAIL
    // -------------------------------------------------------
    @Test
    void shouldNotAllowDuplicatedEmail() {
        repo.save(build("User1", "dup@test.com", true));

        Student duplicated = build("User2", "dup@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(duplicated))
                .isInstanceOf(Exception.class);
    }



    // -------------------------------------------------------
    // 8. VALIDACIONES DE CAMPOS NULL
    // -------------------------------------------------------
    @Test
    void shouldFailWhenFullNameIsNull() {
        Student s = build(null, "nullname@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        Student s = build("Sin Nombre", null, true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    // -------------------------------------------------------
    // 9. EMAIL LENGTH > 120 (rompe la BD)
    // -------------------------------------------------------
    @Test
    void shouldFailWithLongEmail() {
        String longEmail = "a".repeat(130) + "@test.com";

        Student s = build("Usuario Largo", longEmail, true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    // -------------------------------------------------------
    // 10. NOMBRE LENGTH > 120
    // -------------------------------------------------------
    @Test
    void shouldFailWithLongName() {
        String longName = "x".repeat(200);

        Student s = build(longName, "long@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    // -------------------------------------------------------
    // 11. TEST DE DOBLE GUARDADO (simula un update)
    // -------------------------------------------------------
    @Test
    void shouldUpdateStudent() {
        Student s = repo.save(build("Original", "update@test.com", true));

        s.setFullName("Updated Name");

        Student updated = repo.save(s);

        assertThat(updated.getFullName()).isEqualTo("Updated Name");
    }

    // -------------------------------------------------------
    // 12. EMAIL MATCH EXACTO (no ignora mayúsculas)
    // -------------------------------------------------------
    @Test
    void shouldNotMatchEmailWithDifferentCase() {
        repo.save(build("Case Test", "case@test.com", true));

        var result = repo.findByEmail("CASE@test.com");

        assertThat(result).isNotPresent();
    }
}
