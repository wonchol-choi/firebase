package com.example.firebaseex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    EditText edtID, edtName, edtEmail, edtTel;
    Button btnInsert, btnUpdate;
    ListView listView1;
    String id, name, email, tel;
    String sort = "id";
    ArrayAdapter<String> adapter;
    static ArrayList<String> index = new ArrayList<String>();
    static ArrayList<String> data = new ArrayList<String>();
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtID = findViewById(R.id.edtID);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtTel = findViewById(R.id.edtTel);
        btnInsert = findViewById(R.id.btnInsert);
        btnUpdate = findViewById(R.id.btnUpdate);
        listView1 = findViewById(R.id.listView1);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView1.setAdapter(adapter);
        getFirebaseDatabase();
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = edtID.getText().toString();
                name = edtName.getText().toString();
                email = edtEmail.getText().toString();
                tel = edtTel.getText().toString();
                if (!isExistID()) {
                    postFirebaseDatabase(true);
                    getFirebaseDatabase();
                    edtID.setText("");
                    edtName.setText("");
                    edtEmail.setText("");
                    edtTel.setText("");
                    edtID.requestFocus();
                } else {
                    showToast("이미 존재하는 ID입니다. 다른 ID로 설정해 주세요");
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = edtID.getText().toString();
                name = edtName.getText().toString();
                email = edtEmail.getText().toString();
                tel = edtTel.getText().toString();
                if (isExistID()) {
                    postFirebaseDatabase(true);
                    getFirebaseDatabase();
                } else {
                    showToast("수정할 ID를 찾지 못했습니다.");
                }
            }
        });
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("데이터 삭제");
                String nowData[]=data.get(position).split("\\s+");
                builder.setMessage("아이디가 " + nowData[0]+"인 자료를 삭제하시겠습니까?");
                builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postFirebaseDatabase(false);
                        getFirebaseDatabase();
                        showToast("데이터를 삭제했습니다.");
                    }
                });
                builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("삭제를 취소했습니다.");
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                return false;
            }
        });
    }//onCreate 메서드 끝~~

    //토스트 메서드
    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //중복 검사 메서드
    public boolean isExistID() {
        boolean isExist = index.contains(id);
        return isExist;
    }

    //firebaseDB 가져오기 메서드
    public void getFirebaseDatabase() {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data.clear();
                index.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    FirebasePost post = dataSnapshot.getValue(FirebasePost.class);
                    String result = post.id + "  " + post.name + "  " + post.email + "  " + post.tel;
                    data.add(result);
                    index.add(key);
                }
                adapter.clear();
                adapter.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        Query sortid=FirebaseDatabase.getInstance().getReference().child("data_list").orderByChild(sort);
        sortid.addListenerForSingleValueEvent(eventListener);
    }//getFirebaseDatabase() 끝~~

    //firebase 저장하기, 수정하기, 삭제하기 메서드
    public void postFirebaseDatabase(boolean add) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> toMap = new HashMap<>();
        Map<String, Object> value = null;
        if (add) {
            FirebasePost post = new FirebasePost(id, name, email, tel);
            value = post.toMap();
        }
        toMap.put("/data_list/" + id, value); //value가 존재하면 update, 없으면 insert,
        databaseReference.updateChildren(toMap); //if안에 수행을 안하면 기존 데이터 새로고침  }
    }
}