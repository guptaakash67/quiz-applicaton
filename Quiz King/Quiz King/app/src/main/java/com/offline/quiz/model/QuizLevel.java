package com.offline.quiz.model;

import java.util.Collections;
import java.util.List;

import com.offline.quiz.Constant;
import com.offline.quiz.adapter.DBHelper;

public class QuizLevel {
    private int levelNo;
    private int noOfQuestion;
    private List<Quizplay> question;
    DBHelper questionsDao;

    public QuizLevel(int levelNo, int noOfQuestion, DBHelper questionsDao) {
        super();
        this.levelNo = levelNo;
        this.noOfQuestion = noOfQuestion;
        this.questionsDao = questionsDao;
    }

    public int getLevelNo() {
        return levelNo;
    }

    public int getNoOfQuestion() {
        return noOfQuestion;
    }

    public List<Quizplay> getQuestion() {
        return question;
    }

    public void setQuestionGuj() {
        question = questionsDao.getQuestionGuj(Constant.categoryId,Constant.subCategoryId,getNoOfQuestion(), getLevelNo());
//        question = questionsDao.getQuestionGujSingleCat(Constant.categoryId,getNoOfQuestion(), getLevelNo());
        Collections.shuffle(question);
    }
}
