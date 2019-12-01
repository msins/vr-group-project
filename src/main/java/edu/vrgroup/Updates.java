package edu.vrgroup;

import edu.vrgroup.model.Answer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Updates {

  private Deque<Answer> updates = new ArrayDeque<>();
  private List<UpdateListener> listeners = new CopyOnWriteArrayList<>();

  public interface UpdateListener {

    void onUpdate(Answer answer);
  }

  public void add(Answer answer) {
    updates.addFirst(answer);
    listeners.forEach(e -> e.onUpdate(answer));
  }

  public List<Answer> getHistory(int limit) {
    return updates.stream().limit(limit).collect(Collectors.toList());
  }

  public void addUpdateListener(UpdateListener listener) {
    listeners.add(listener);
  }

  public void removeUpdateListener(UpdateListener listener) {
    listeners.remove(listener);
  }

}
