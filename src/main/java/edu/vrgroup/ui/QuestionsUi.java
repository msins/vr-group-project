package edu.vrgroup.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.vrgroup.GameChangeListener;
import edu.vrgroup.ScenarioChangeListener;
import edu.vrgroup.database.DaoProvider;
import edu.vrgroup.model.Answer;
import edu.vrgroup.model.Choice;
import edu.vrgroup.model.Game;
import edu.vrgroup.model.Question;
import edu.vrgroup.model.Scenario;
import edu.vrgroup.ui.forms.NewQuestionForm;
import edu.vrgroup.ui.providers.AnswersGridDataProvider;
import edu.vrgroup.ui.providers.QuestionsProvider;
import edu.vrgroup.ui.util.AbstractButtonFactory;
import edu.vrgroup.ui.util.AnswersGrid;
import edu.vrgroup.ui.util.ResultChart;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Route(value = "questions", layout = MainAppUi.class)
@PageTitle("Questions")
public class QuestionsUi extends HorizontalLayout implements GameChangeListener, ScenarioChangeListener {

  private Game game;
  private Scenario scenario;
  private Questions questions;
  private QuestionView questionInformation;

  public QuestionsUi() {
    questions = new Questions();

    if (game != null) {
      questions.setDataProvider(new QuestionsProvider(scenario));
    }
    setSizeFull();

    questions.addValueChangeListener(list -> {
      QuestionView old = questionInformation;
      if (list.getValue() != null) {
        questionInformation = new QuestionView(
            this.game,
            this.scenario,
            list.getValue(),
            questions.getDataProvider());
      }

      replace(old, questionInformation);
      add(questionInformation);
    });

    Button button = new Button("New question", VaadinIcon.PLUS.create(),
        e -> new NewQuestionForm(scenario, question -> questions.getDataProvider().refreshAll()).open()) {{
      setWidthFull();
    }};

    add(new VerticalLayout(button, questions) {{
      setMaxWidth("25%");
      setMinWidth("25%");
    }});
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    registerToGameNotifier();
    registerToScenarioNotifier(game);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    unregisterFromGameNotifier();
    unregisterFromScenarioNotifier(game);
  }

  @Override
  public void gameChanged(Game game) {
    this.game = game;
    if (scenario != null) {
      questions.setDataProvider(new QuestionsProvider(scenario));
    }
    questions.getDataProvider().refreshAll();
    if (questionInformation != null) {
      questionInformation.removeAll();
    }
  }

  @Override
  public void scenarioChanged(Scenario scenario) {
    this.scenario = scenario;
    if (questionInformation != null) {
      questionInformation.setScenario(scenario);
    }
  }

  private static class Questions extends ListBox<Question> {

    {
      getElement().getStyle().set("border-style", "groove");
      getElement().getStyle().set("border-width", "thin");
      getElement().getStyle().set("border-color", "var(--lumo-primary-color-10pct)");
      getElement().getStyle().set("border-radius", "var(--lumo-border-radius-m)");
      getElement().getStyle().set("box-shadow", "var(--lumo-boc-shadow-s)");
      setHeightFull();
      setRenderer(new TextRenderer<>(Question::getText));
      setWidthFull();
    }

    public Questions() {
    }
  }

  private static class QuestionView extends VerticalLayout {

    private ResultChart<Question> chart;
    private Grid<Answer> grid;
    private Game game;
    private Scenario scenario;
    private Question question;
    private DataProvider<Question, ?> notifier;

    public QuestionView(Game game, Scenario scenario, Question question, DataProvider<Question, ?> notifier) {
      this.game = game;
      this.scenario = scenario;
      this.question = question;
      this.notifier = notifier;
      initUi();
    }

    public void setGame(Game game) {
      this.game = game;
    }

    public void setScenario(Scenario scenario) {
      this.scenario = scenario;
    }

    public void setQuestion(Question question) {
      this.question = question;
    }

    private void initUi() {
      chart = new ResultChart<>(question);
      chart.setWidthFull();

      grid = new QuestionAnswerGrid(game, scenario, question);
      grid.setWidthFull();

      VerticalLayout layout = new VerticalLayout();
      layout.setAlignItems(Alignment.CENTER);

      HorizontalLayout gridChart = new HorizontalLayout(grid, chart);
      gridChart.setWidthFull();
      layout.add(gridChart);

      TextArea text = new TextArea("Text");
      text.setValue(question.getText());
      text.setWidthFull();
      layout.add(text);

      Button refresh = new Button(VaadinIcon.REFRESH.create(), e -> {
        List<Answer> answers = DaoProvider.getDao().getAllAnswers(game, scenario, question);
        Number[] stats = QuestionStatistics.get(answers, question);
        chart.refresh(stats);
        grid.getDataProvider().refreshAll();
      });
      refresh.click();

      Button delete = AbstractButtonFactory.getRectangle().createRedButton("Delete", e -> {
        DaoProvider.getDao().removeQuestion(question);
        removeAll();
        notifier.refreshAll();
      });

      Button sync = AbstractButtonFactory.getRectangle().createGreenButton("Sync",
          e -> {
            DaoProvider.getDao().updateQuestion(question, text.getValue());
            notifier.refreshAll();
          });
      sync.setEnabled(false);
      text.addKeyPressListener(e -> sync.setEnabled(true));

      layout.add(new HorizontalLayout(refresh, delete, sync));
      add(layout);
    }
  }

  private static final class QuestionStatistics {

    public static Number[] get(List<Answer> answers, Question question) {
      Map<Choice, Integer> counter = new TreeMap<>();
      question.getChoices().forEach(c -> counter.put(c, 0));
      answers.forEach(a -> counter.merge(a.getChoice(), 1, Integer::sum));
      return counter.values().stream()
          .map(value -> (value.doubleValue() / answers.size()) * 100)
          .toArray(Number[]::new);
    }
  }

  private static class QuestionAnswerGrid extends AnswersGrid {

    private Game game;
    private Scenario scenario;
    private Question question;

    public QuestionAnswerGrid(Game game, Scenario scenario, Question question) {
      this.game = game;
      this.scenario = scenario;
      this.question = question;
    }

    private void initUi() {
      addColumn(a -> a.getTimestamp().toLocalDateTime()
          .format(DateTimeFormatter.ofPattern("d.MM.yyyy. HH:mm:ss")))
          .setHeader(new Html("<b>Time</b>"));
      addColumn(
          TemplateRenderer.<Answer>of("<div>[[item.user.name]]<b>[</b>[[item.IPv4]]<b>]</b></div>")
              .withProperty("user", Answer::getUser)
              .withProperty("IPv4", Answer::getIPv4))
          .setHeader(new Html("<b>User</b>"));
      addColumn(Answer::getChoice)
          .setHeader(new Html("<b>Score</b>"));

      getColumns().forEach(e -> e.setAutoWidth(true));
      getColumns().forEach(e -> e.setSortable(true));
      setDataProvider(new AnswersGridDataProvider(game, scenario, question));
    }
  }
}
