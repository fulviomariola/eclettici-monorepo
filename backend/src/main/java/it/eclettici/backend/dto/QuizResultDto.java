package it.eclettici.backend.dto;

public class QuizResultDto {
    private int score;
    private boolean passed;
    private int passingScore;
    private String message;

    public QuizResultDto() {}
    public QuizResultDto(int score, boolean passed, int passingScore, String message) {
        this.score = score;
        this.passed = passed;
        this.passingScore = passingScore;
        this.message = message;
    }

    public int getScore() { return score; }
    public boolean isPassed() { return passed; }
    public int getPassingScore() { return passingScore; }
    public String getMessage() { return message; }
}