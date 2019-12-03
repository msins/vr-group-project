package edu.vrgroup.database;

import edu.vrgroup.model.Answer;
import edu.vrgroup.model.Choice;
import edu.vrgroup.model.Game;
import edu.vrgroup.model.GameQuestion;
import edu.vrgroup.model.Question;
import edu.vrgroup.model.Scenario;
import edu.vrgroup.questions.MultipleChoicesQuestion;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

public class JpaDaoImpl implements Dao {

  @Override
  @SuppressWarnings("unchecked")
  public List<Question> getQuestions(Game game) {
    List<Question> results = (List<Question>) getEntityManager()
        .createQuery(
            "select q from Question as q, GameQuestion as gq where q.id = gq.question.id and gq.game.id = :gameId")
        .setParameter("gameId", game.getId())
        .getResultList();
    return results;
  }

  @Override
  public void addQuestion(Game game, Question question) {
    getEntityManager().persist(question);
    for (var choice : question.getChoices()) {
      getEntityManager().persist(choice);
    }
    getEntityManager().persist(new GameQuestion(game, question));
    getEntityManager().getTransaction().commit();
  }

  @Override
  public int getQuestionsCount(Game game) {
    return ((Long) getEntityManager()
        .createQuery(
            "select count(*) from Question as q, GameQuestion as gq where q.id = gq.question.id and gq.game.id = :gameId")
        .setParameter("gameId", game.getId())
        .getSingleResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Choice> getChoices(MultipleChoicesQuestion question) {
    List<Choice> results = (List<Choice>) getEntityManager()
        .createQuery("select choice from Choice as choice where choice.questionId = :questionId")
        .setParameter("questionId", question.getId())
        .setMaxResults(50)
        .getResultList();

    return results;
  }

  @Override
  public int getGamesCount() {
    return ((Long) getEntityManager()
        .createQuery("select count(*) from Game")
        .getSingleResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Game> getGames() {
    List<Game> results = (List<Game>) getEntityManager()
        .createQuery("select game from Game as game")
        .setMaxResults(50)
        .getResultList();

    return results;
  }

  @Override
  public boolean containsGame(Game game) {
    return ((Long) getEntityManager().createQuery("select count(game.id) from Game as game where game.id = :gameId")
        .setParameter("gameId", game.getId()).getSingleResult()) != 0;
  }

  @Override
  public void addGame(Game game) {
    getEntityManager().persist(game);
    getEntityManager().getTransaction().commit();
  }

  @Override
  public void removeGame(Game game) {
    EntityManager em = getEntityManager();
    em.remove(em.contains(game) ? game : em.merge(game));
    em.getTransaction().commit();
  }

  @Override
  public int getAnswersCount() {
    return ((Long) getEntityManager()
        .createQuery("select count(*) from Answer")
        .getSingleResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Answer> getAnswers(int offset, int limit) {
    List<Answer> results = (List<Answer>) getEntityManager()
        .createQuery("select answer from Answer as answer")
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();

    return results;
  }

  @Override
  public int getAnswersCount(Game game) {
    if (game == null) {
      return getAnswersCount();
    }
    return ((Long) getEntityManager()
        .createQuery("select count(*) from Answer as answer where answer.game.id = :gameId")
        .setParameter("gameId", game.getId())
        .getSingleResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Answer> getAnswers(Game game, int offset, int limit) {
    if (game == null) {
      return getAnswers(offset, limit);
    }
    List<Answer> results = (List<Answer>) getEntityManager()
        .createQuery("select answer from Answer as answer where answer.game.id = :gameId")
        .setParameter("gameId", game.getId())
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();

    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Answer> getAnswers(Game game, Scenario scenario, Question question, int offset, int limit) {
    List<Answer> results = (List<Answer>) getEntityManager()
        .createQuery(
            "select answer from Answer as answer where answer.game.id = :gameId and answer.scenario.id = :scenarioId and answer.question.id = :questionId")
        .setParameter("gameId", game.getId())
        .setParameter("scenarioId", scenario.getId())
        .setParameter("questionId", question.getId())
        .getResultList();
    return results;
  }

  @Override
  public int getAnswersCount(Game game, Scenario scenario, Question question) {
    return ((Long) getEntityManager()
        .createQuery(
            "select count(*) from Answer as answer where answer.game.id = :gameId and answer.scenario.id = :scenarioId and answer.question.id = :questionId")
        .setParameter("gameId", game.getId())
        .setParameter("scenarioId", scenario.getId())
        .setParameter("questionId", question.getId())
        .getSingleResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Choice, Integer> getQuestionStatistics(Game game, Scenario scenario, Question question) {
    Object a = getEntityManager()
        .createQuery(
            "select score, count(*) from Answer as answer where answer.game.id = :gameId and answer.scenario.id = :scenarioId and answer.question.id = :questionId group by score")
        .setParameter("gameId", game.getId())
        .setParameter("scenarioId", scenario.getId())
        .setParameter("questionId", question.getId())
        .getSingleResult();
    return null;
  }

  private EntityManager getEntityManager() {
    return JpaEntityManagerProvider.getEntityManager();
  }
}
