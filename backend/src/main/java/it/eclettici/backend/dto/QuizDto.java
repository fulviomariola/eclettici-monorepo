package it.eclettici.backend.dto;

import java.util.List;

public class QuizDto {
    private Long id;
    private String title;
    private int passingScore;
    private List<QuestionDto> questions;

    public QuizDto() {}

    public QuizDto(Long id, String title, int passingScore, List<QuestionDto> questions) {
        this.id = id;
        this.title = title;
        this.passingScore = passingScore;
        this.questions = questions;
    }

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

        public OptionDto() {}
        public OptionDto(Long id, String optionText) {
            this.id = id;
            this.optionText = optionText;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
}