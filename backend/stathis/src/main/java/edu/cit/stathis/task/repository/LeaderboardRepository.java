package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.Leaderboard;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {
    List<Leaderboard> findByTaskIdOrderByScoreDescTimeTakenAsc(String taskId);
    List<Leaderboard> findByStudentId(String studentId);
    Leaderboard findByPhysicalId(String physicalId);
    boolean existsByPhysicalId(String physicalId);
    Leaderboard findByStudentIdAndTaskId(String studentId, String taskId);
} 