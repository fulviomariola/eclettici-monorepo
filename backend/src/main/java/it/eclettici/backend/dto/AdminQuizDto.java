package it.eclettici.backend.dto;

import java.util.List;

public class AdminQuizDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private int passingScore;
    private List<QuestionDto> questions;

    public AdminQuizDto() {}

    public AdminQuizDto(Long id, Long courseId, String courseTitle, String title, int passingScore, List<QuestionDto> questions) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.passingScore = passingScore;
        this.questions = questions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }

    public static class QuestionDto {
        private Long id;
        private String questionText;
        private List<OptionDto> options;

        public QuestionDto() {}

        public QuestionDto(Long id, String questionText, List<OptionDto> options) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public List<OptionDto> getOptions() { return options; }
        public void setOptions(List<OptionDto> options) { this.options = options; }
    }

    public static class OptionDto {
        private Long id;
        private String optionText;
        private boolean isCorrect;

        public OptionDto() {}

        public OptionDto(Long id, String optionText, boolean isCorrect) {
            this.id = id;
            this.optionText = optionText;
            this.isCorrect = isCorrect;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }
}