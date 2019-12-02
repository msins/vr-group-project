package edu.vrgroup.questions;

import com.google.common.base.MoreObjects;
import edu.vrgroup.model.Choice;
import edu.vrgroup.model.Question;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity(name = "ScalingQuestion")
@PrimaryKeyJoinColumn(name = "questionId")
public class ScalingQuestion extends Question {

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("text", text)
        .add("type", type)
        .toString();
  }

  @Override
  public List<Choice> getChoices() {
    return null;
  }
}
