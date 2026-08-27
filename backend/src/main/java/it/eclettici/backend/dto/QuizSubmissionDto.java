package it.eclettici.backend.dto;

import java.util.List;

public class QuizSubmissionDto {
    private List<AnswerDto> answers;

    public static class AnswerDto {
        private Long questionId;
        private Long selectedOptionId;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    }

    public List<AnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDto> answers) { this.answers = answers; }
}