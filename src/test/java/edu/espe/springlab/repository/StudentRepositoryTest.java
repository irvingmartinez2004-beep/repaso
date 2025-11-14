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
    // 1. SAVE
    // -------------------------------------------------------
    @Test
    void shouldSaveStudent() {
        Student saved = repo.save(build("Juan Perez", "juan@test.com", true));

        assertThat(saved.getId()).isNotNull();
    }

    /*
    @Test // ❌ INCORRECTO → Falta verificar ID
    void saveWithoutAssertions() {
        repo.save(build("Name", "email@test.com", true));
    }
    */

    // -------------------------------------------------------
    // 2. FIND BY EMAIL
    // -------------------------------------------------------
    @Test
    void shouldSaveAndFindByEmail() {
        repo.save(build("Maria Lopez", "maria@test.com", true));

        var result = repo.findByEmail("maria@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Maria Lopez");
    }

    /*
    @Test // ❌ INCORRECTO → Busca un email que nunca se guardó
    void wrongFindByEmail() {
        assertThat(repo.findByEmail("wrong@mail.com")).isPresent();
    }
    */

    // -------------------------------------------------------
    // 3. EXISTS BY EMAIL
    // -------------------------------------------------------
    @Test
    void shouldReturnTrueIfEmailExists() {
        repo.save(build("User A", "exists@test.com", true));

        assertThat(repo.existsByEmail("exists@test.com")).isTrue();
    }

    /*
    @Test // ❌ INCORRECTO → Asume que siempre existe
    void wrongExistsByEmail() {
        assertThat(repo.existsByEmail("nope@mail.com")).isTrue();
    }
    */

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() {
        assertThat(repo.existsByEmail("no@test.com")).isFalse();
    }

    /*
    @Test // ❌ INCORRECTO → No se valida nada
    void wrongExistsWithoutAssert() {
        repo.existsByEmail("anything@mail.com");
    }
    */

    // -------------------------------------------------------
    // 4. FIND BY ID
    // -------------------------------------------------------
    @Test
    void shouldFindById() {
        Student s = repo.save(build("Ana", "ana@test.com", true));

        var found = repo.findById(s.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ana@test.com");
    }

    /*
    @Test // ❌ INCORRECTO → ID inexistente pero espera resultado
    void wrongFindById() {
        assertThat(repo.findById(999L)).isPresent();
    }
    */

    @Test
    void shouldReturnEmptyIfNotFoundById() {
        assertThat(repo.findById(999L)).isNotPresent();
    }

    /*
    @Test // ❌ INCORRECTO → Debería ser notPresent()
    void wrongAssertPresent() {
        assertThat(repo.findById(999L)).isPresent();
    }
    */

    // -------------------------------------------------------
    // 5. FIND ALL
    // -------------------------------------------------------
    @Test
    void shouldFindAllStudents() {
        repo.save(build("A", "a@mail.com", true));
        repo.save(build("B", "b@mail.com", true));

        List<Student> list = repo.findAll();

        assertThat(list).hasSize(2);
    }

    /*
    @Test // ❌ INCORRECTO → No verifica el tamaño
    void wrongFindAll() {
        repo.findAll();
    }
    */

    @Test
    void shouldReturnEmptyListIfNoStudents() {
        assertThat(repo.findAll()).isEmpty();
    }

    /*
    @Test // ❌ INCORRECTO → Debería ser empty, no size 1
    void wrongEmptyList() {
        assertThat(repo.findAll()).hasSize(1);
    }
    */

    // -------------------------------------------------------
    // 6. DELETE
    // -------------------------------------------------------
    @Test
    void shouldDeleteById() {
        Student s = repo.save(build("Delete User", "delete@test.com", true));

        repo.deleteById(s.getId());

        assertThat(repo.findById(s.getId())).isNotPresent();
    }

    /*
    @Test // ❌ INCORRECTO → No se elimina nada
    void wrongDeleteWithoutSaving() {
        repo.deleteById(1L);
    }
    */

    // -------------------------------------------------------
    // 7. UNIQUE EMAIL
    // -------------------------------------------------------
    @Test
    void shouldNotAllowDuplicatedEmail() {
        repo.save(build("User1", "dup@test.com", true));

        Student duplicated = build("User2", "dup@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(duplicated))
                .isInstanceOf(Exception.class);
    }

    /*
    @Test // ❌ INCORRECTO → Aquí no hacemos flush y podría NO fallar
    void wrongDuplicateTest() {
        repo.save(build("A", "x@test.com", true));
        repo.save(build("B", "x@test.com", true)); // Esto a veces NO lanza error
    }
    */

    // -------------------------------------------------------
    // 8. VALIDACIONES DE CAMPOS NULL
    // -------------------------------------------------------
    @Test
    void shouldFailWhenFullNameIsNull() {
        Student s = build(null, "nullname@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    /*
    @Test // ❌ INCORRECTO → Sí guarda, pero no se valida nada
    void wrongNullName() {
        repo.save(build(null, "x@test.com", true));
    }
    */

    @Test
    void shouldFailWhenEmailIsNull() {
        Student s = build("Sin Nombre", null, true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    /*
    @Test // ❌ INCORRECTO → Espera que exista
    void wrongNullEmail() {
        assertThat(repo.save(build("A", null, true)).getEmail()).isNotNull();
    }
    */

    // -------------------------------------------------------
    // 9. CAMPOS MUY LARGOS
    // -------------------------------------------------------
    @Test
    void shouldFailWithLongEmail() {
        String longEmail = "a".repeat(130) + "@test.com";

        Student s = build("Usuario Largo", longEmail, true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    /*
    @Test // ❌ INCORRECTO → Ignora las reglas de longitud
    void wrongEmailLength() {
        repo.save(build("User", "a".repeat(200), true));
    }
    */

    @Test
    void shouldFailWithLongName() {
        String longName = "x".repeat(200);

        Student s = build(longName, "long@test.com", true);

        assertThatThrownBy(() -> repo.saveAndFlush(s))
                .isInstanceOf(Exception.class);
    }

    /*
    @Test // ❌ INCORRECTO → Este nombre debería fallar
    void wrongNameLength() {
        repo.save(build("x".repeat(200), "test@mail.com", true));
    }
    */

    // -------------------------------------------------------
    // 10. UPDATE (TEST COMPLETO)
    // -------------------------------------------------------
    @Test
    void shouldUpdateStudentCorrectly() {
        Student s = repo.save(build("Original", "update@test.com", true));

        s.setFullName("Updated Name");
        s.setEmail("updated@test.com");
        s.setActive(false);

        Student updated = repo.saveAndFlush(s);

        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
        assertThat(updated.getActive()).isFalse();
    }

    /*
    @Test // ❌ INCORRECTO → No actualiza nada
    void wrongUpdate() {
        Student s = repo.save(build("A", "a@test.com", true));
        s.setFullName("Changed");
        // Falta repo.save(s)
        assertThat(s.getFullName()).isEqualTo("Changed");
    }
    */

    // -------------------------------------------------------
    // 11. CASE SENSITIVITY
    // -------------------------------------------------------
    @Test
    void shouldNotMatchEmailWithDifferentCase() {
        repo.save(build("Case Test", "case@test.com", true));

        var result = repo.findByEmail("CASE@test.com");

        assertThat(result).isNotPresent();
    }

    /*
    @Test // ❌ INCORRECTO → Esto NUNCA debería funcionar
    void wrongCaseInsensitiveTest() {
        repo.save(build("Case", "case@test.com", true));
        assertThat(repo.findByEmail("CASE@test.com")).isPresent();
    }
    */
}
