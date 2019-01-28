package bytedance.com.tikshow.antoniolq;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collections;

import bytedance.com.tikshow.HoqiheChen.VideoPlay.MainPageActivity;
import bytedance.com.tikshow.HoqiheChen.VideoPlay.MyLayoutManager;
import bytedance.com.tikshow.R;

public class Login extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginBtn;
    private int flag = 0;

    public SQLiteDatabase sqliteDatabase;
    public TodoDbHelper mtodobHelper;
    public Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mtodobHelper = new TodoDbHelper(Login.this);
        sqliteDatabase = mtodobHelper.getWritableDatabase();
        sqliteDatabase.execSQL("INSERT INTO user (name,password)\n" + "VALUES ('lq',123);");
        sqliteDatabase.execSQL("INSERT INTO user (name,password)\n" + "VALUES ('gc',123);");
        username = findViewById(R.id.user);
        password =findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sqliteDatabase == null){
                    Toast.makeText(Login.this,
                            "建表失败", Toast.LENGTH_SHORT).show();
                }
                if(username != null && password != null){
                    try {
                        cursor = sqliteDatabase.rawQuery("select * from user order by ID ",null);
                        while (cursor.moveToNext())
                        {
                            String _username = cursor.getString(cursor.getColumnIndex("name"));
                            int _password = cursor.getInt(cursor.getColumnIndex("password"));

                            if(_username.equals(username.getText().toString()) && String.valueOf(_password).equals(password.getText().toString()))
                            {
                                flag = 1;
                            }
                        }
                    }finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    if(flag == 1)
                    {
                        Toast.makeText(Login.this,
                                "登陆成功,积分+5!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this,MainPageActivity.class));
                        finish();
                    }
                    else
                    {
                        Toast.makeText(Login.this,
                                "用户名或密码错误，请重试！", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(Login.this,
                            "请输入您的用户名和密码！", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
