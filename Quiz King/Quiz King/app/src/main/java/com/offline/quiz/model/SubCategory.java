package com.offline.quiz.model;

import java.util.ArrayList;

public class SubCategory {
    private int id;
    private String  name, image, categoryId;
    int totalLevel;
    public ArrayList<Quizplay> questionList;

    public SubCategory() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setTotalLevel(int totalLevel) {
        this.totalLevel = totalLevel;
    }

    public ArrayList<Quizplay> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(ArrayList<Quizplay> questionList) {
        this.questionList = questionList;
    }
}
