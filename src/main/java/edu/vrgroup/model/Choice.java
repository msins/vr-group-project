package edu.vrgroup.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "Choice")
public class Choice implements Serializable, Comparable<Choice> {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Expose
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "questionId", nullable = false, foreignKey = @ForeignKey(
      foreignKeyDefinition = "FOREIGN KEY (questionId) REFERENCES Question(id) ON DELETE CASCADE ON UPDATE CASCADE"
  ))
  private Question question;

  @Column(name = "text")
  @Expose
  private String value;

  @Column(name = "orderValue")
  private Integer order;

  public Choice() {

  }

  public Choice(Question question, String value, Integer order) {
    this.question = question;
    this.value = value;
    this.order = order;
  }

  public Integer getId() {
    return id;
  }

  public Integer getOrder() {
    return order;
  }

  public Question getQuestion() {
    return question;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Choice choice = (Choice) o;
    return id.equals(choice.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public int compareTo(Choice other) {
    return Integer.compare(this.order, other.order);
  }
}
