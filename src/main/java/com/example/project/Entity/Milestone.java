package com.example.project.Entity;

import com.example.project.Enum.MilestoneStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Enumerated(EnumType.STRING)
    private MilestoneStatus status;
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;
    @OneToMany(
            mappedBy = "milestone",
            cascade = CascadeType.ALL
    )
    private List<FocusSession> focusSessions = new ArrayList<>();
}

