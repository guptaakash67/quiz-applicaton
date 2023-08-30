package com.offline.quiz.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.offline.quiz.Constant;
import com.offline.quiz.model.Category;
import com.offline.quiz.model.Quizplay;
import com.offline.quiz.model.SubCategory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private String packageName;
    private SQLiteDatabase db;
    private static final String db_name = "quiz_main67.db";

    //table names
    public static final String TBL_CATEGORY = "tbl_category";
    public static final String TBL_SUB_CATEGORY = "tbl_subCategory";
    public static final String TBL_QUESTION = "questions_list";
    //table name
    public static String TBL_LEVEL = "tbl_level";

    //column names
    public static String LEVEL_NO = "level_no";
    public static final String ID = "id";
    public static final String CATE_ID = "cate_id";
    public static final String SUB_CATE_ID = "sub_cate_id";
    public static final String CATEGORY_NAME = "category";
    public static final String SUB_CATEGORY_NAME = "sub_category";
    public static final String QUESTION_SOLUTION = "que_solution";
    public static final String QUESTION = "question";
    public static final String OPTION_A = "option_a";
    public static final String OPTION_B = "option_b";
    public static final String OPTION_C = "option_c";
    public static final String OPTION_D = "option_d";
    public static final String RIGHT_ANSWER = "right_answer";
    public static final String LEVEL = "level";


    private String db_path;
    private static int db_version = 1;
    Context con;


    public DBHelper(Context con) {
        super(con, db_name, null, db_version);
        // TODO Auto-generated constructor stub

        db_path= con.getDatabasePath(db_name).getAbsolutePath();
        //db_path = con.getDatabasePath(db_name).toString().replace(db_name, "");
        this.con = con;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    public void createDB() throws IOException {

        if (checkDB()) {
        } else if (!checkDB()) {
            this.getReadableDatabase();
            close();
            copyDB();
        }

    }

    private boolean checkDB() {

        SQLiteDatabase cDB = null;
        try {
            cDB = SQLiteDatabase.openDatabase(db_path, null,
                    SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (cDB != null) {
            cDB.close();
        }
        return cDB != null ? true : false;
    }


    private void copyDB() throws IOException {
        InputStream inputFile = con.getAssets().open(db_name);
      //  String outFileName = db_path + db_name;
        OutputStream outFile = new FileOutputStream(db_path);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputFile.read(buffer)) > 0) {
            outFile.write(buffer, 0, length);
        }
        outFile.flush();
        outFile.close();
        inputFile.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub


    }

    /*
     *get All category from table
     */
    public ArrayList<Category> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        Cursor cur = db.rawQuery("SELECT * FROM  " + TBL_CATEGORY, null);
        if (cur.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cur.getInt(cur.getColumnIndex(ID)));
                category.setName(cur.getString(cur.getColumnIndex(CATEGORY_NAME)));
                categoryArrayList.add(category);

            } while (cur.moveToNext());
        }
        //}
        return categoryArrayList;
    }

    public int GetMaxLevelSingleCat(int cat_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select max(" + LEVEL + ") from " + TBL_QUESTION + " where (" + CATE_ID + "=" + cat_id + ")", null);
        if (cur.moveToFirst()) {
            do {
                Constant.totalLevel = cur.getInt(cur.getColumnIndex("max(level)"));
            } while (cur.moveToNext());
        }
        //}
        return Constant.totalLevel;
    }

    public int GetMaxLevel(int cat_id, int sub_cate_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select max(" + LEVEL + ") from " + TBL_QUESTION + " where (" + CATE_ID + "=" + cat_id + " and " + SUB_CATE_ID + "=" + sub_cate_id + ")", null);
        if (cur.moveToFirst()) {
            do {
                Constant.totalLevel = cur.getInt(cur.getColumnIndex("max(level)"));
            } while (cur.moveToNext());
        }
        //}
        return Constant.totalLevel;
    }

    public ArrayList<SubCategory> getSubCategoryById(int cate_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<SubCategory> subCategories = new ArrayList<>();
        Cursor cur = db.rawQuery("SELECT * FROM  " + TBL_SUB_CATEGORY + " where (" + CATE_ID + " = " + cate_id + ")", null);
        if (cur.moveToFirst()) {
            do {
                SubCategory subCategory = new SubCategory();
                subCategory.setId(cur.getInt(cur.getColumnIndex(ID)));
                subCategory.setCategoryId(cur.getString(cur.getColumnIndex(CATE_ID)));
                subCategory.setName(cur.getString(cur.getColumnIndex(SUB_CATEGORY_NAME)));
                subCategories.add(subCategory);

            } while (cur.moveToNext());
        }
        //}
        return subCategories;
    }

    public List<Quizplay> getQuestionGujSingleCat(int cate_id, int noOfQuestion, int level) {

        List<Quizplay> quizplay = new ArrayList<Quizplay>();
        int total = noOfQuestion;
        String sql = "select *  FROM " + TBL_QUESTION + " where (" + CATE_ID + "=" + cate_id + " and "
                + LEVEL + "=" + level + ") ORDER BY RANDOM() LIMIT " + total;
        SQLiteDatabase db = this.getReadableDatabase();
        //SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME, null, 0);
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Quizplay question = new Quizplay();
                question.setId(cursor.getInt(cursor.getColumnIndex("id")));
                question.setQuestion(cursor.getString(cursor.getColumnIndex("question")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_a")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_b")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_c")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_d")));
                String rightAns = cursor.getString(cursor.getColumnIndex("right_answer"));
                if (rightAns.equalsIgnoreCase("A")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_a")));
                } else if (rightAns.equalsIgnoreCase("B")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_b")));
                } else if (rightAns.equalsIgnoreCase("C")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_c")));
                } else {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_d")));
                }
                if (question.getOptions().size() == 4) {
                    quizplay.add(question);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.shuffle(quizplay);
        quizplay = quizplay.subList(0, noOfQuestion);
        return quizplay;
    }
    public List<Quizplay> getQuestionGuj(int cate_id, int sub_cate_id, int noOfQuestion, int level) {

        List<Quizplay> quizplay = new ArrayList<Quizplay>();
        int total = noOfQuestion;
        String sql = "select *  FROM " + TBL_QUESTION + " where (" + CATE_ID + "=" + cate_id + " and "
                + SUB_CATE_ID + " =" + sub_cate_id + " and "
                + LEVEL + "=" + level + ") ORDER BY RANDOM() LIMIT " + total;
        SQLiteDatabase db = this.getReadableDatabase();
        //SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME, null, 0);
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Quizplay question = new Quizplay();
                question.setId(cursor.getInt(cursor.getColumnIndex("id")));
                question.setQuestion(cursor.getString(cursor.getColumnIndex("question")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_a")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_b")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_c")));
                question.addOption(cursor.getString(cursor.getColumnIndex("option_d")));
                String rightAns = cursor.getString(cursor.getColumnIndex("right_answer"));
                if (rightAns.equalsIgnoreCase("A")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_a")));
                } else if (rightAns.equalsIgnoreCase("B")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_b")));
                } else if (rightAns.equalsIgnoreCase("C")) {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_c")));
                } else {
                    question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_d")));
                }
                if (question.getOptions().size() == 4) {
                    quizplay.add(question);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.shuffle(quizplay);
        quizplay = quizplay.subList(0, noOfQuestion);
        return quizplay;
    }


    /*
     * insert level no
     */
    public void insertIntoDBSingleCat(int cat_id,int level_no) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO " + TBL_LEVEL + " (" + CATE_ID + "," + LEVEL_NO + ") VALUES('" + cat_id + "', '" + level_no + "');";
        db.execSQL(query);

    }
    public void insertIntoDB(int cat_id, int sub_cat_id, int level_no) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO " + TBL_LEVEL + " (" + CATE_ID + "," + SUB_CATE_ID + "," + LEVEL_NO + ") VALUES('" + cat_id + "', '" + sub_cat_id + "', '" + level_no + "');";
        db.execSQL(query);

    }

    /*
     *with this method we check if categoryId & subCategoryId is already exist or not in our database
     */
    public boolean isExistSingleCat(int cat_id) {
        db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TBL_LEVEL + " WHERE ( " + CATE_ID + " = " + cat_id + ")", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();

        return exist;

    }
    public boolean isExist(int cat_id, int sub_cat_id) {
        db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TBL_LEVEL + " WHERE ( " + CATE_ID + " = " + cat_id + " AND " + SUB_CATE_ID + " = " + sub_cat_id + ")", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();

        return exist;

    }

    /*
     * get level
     */
    public int GetLevelByIdUsingSingleCat(int cat_id) {
        int level = 1;
        String selectQuery = "SELECT  * FROM " + TBL_LEVEL + " WHERE  (" + CATE_ID + "=" + cat_id + ")";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                level = c.getInt(c.getColumnIndex(LEVEL_NO));
            } while (c.moveToNext());
        }
        return level;
    }

    public int GetLevelById(int cat_id, int sub_cat_id) {
        int level = 1;
        String selectQuery = "SELECT  * FROM " + TBL_LEVEL + " WHERE  (" + CATE_ID + "=" + cat_id + " AND " + SUB_CATE_ID + "=" + sub_cat_id + ")";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                level = c.getInt(c.getColumnIndex(LEVEL_NO));
            } while (c.moveToNext());
        }
        return level;
    }

    public String getQuestionSolution(int queId) {
        String level = "";
        String selectQuery = "SELECT  * FROM " + TBL_QUESTION + " WHERE  (" + ID + "=" + queId + ")";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        level = c.getString(c.getColumnIndex(QUESTION_SOLUTION));
                    } while (c.moveToNext());
                }


        return level;
    }

    /*
     * Update lavel
     */
//    public void UpdateLevelSingleCat(int cat_id, int level_no) {
//        db = this.getReadableDatabase();
//
//        db.execSQL("update " + TBL_LEVEL + " set level_no=" + level_no + " where (" + CATE_ID + "=" + cat_id + ")");
//    }
    public void UpdateLevel(int cat_id, int sub_cat_id, int level_no) {
        db = this.getReadableDatabase();

        db.execSQL("update " + TBL_LEVEL + " set level_no=" + level_no + " where (" + CATE_ID + "=" + cat_id + "  and  " + SUB_CATE_ID + " = " + sub_cat_id + ")");
    }
}
