package com._project._project.TimeCard;

import com._project._project.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

@Repository
public interface TimeCardRepository extends JpaRepository<TimeCard, Long> {
    TimeCard findById(long id);
    List<TimeCard> findByUser(User user);
    TimeCard findByUserAndClockOutIsNull(User user);
    List<TimeCard> findByUserId(Long userId);
    TimeCard findByUserNumber(long userNumber);
    TimeCard findByUserNumberAndClockOutIsNull(long userNumber);
    List<TimeCard> findByUserNumber(Long userNumber);
    List<TimeCard> findAllByUserNumber(long userNumber);
    TimeCard findFirstByUserNumberOrderByClockInDesc(long userNumber);
    List<TimeCard> findByUserAndWeekNumberAndYear(User user, int weekNumber, int year);
    @Query("SELECT t FROM TimeCard t WHERE t.user.id = ?1 AND t.year = ?2")
    List<TimeCard> findByUserAndYear(long userId, int year);
} 