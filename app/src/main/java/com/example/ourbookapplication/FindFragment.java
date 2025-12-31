package com.example.ourbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class FindFragment extends Fragment {

    private EditText etSearchTitle, etSearchLocation;
    private Button btnSearch;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsAdapter searchAdapter;
    private List<Book> allBooks = new ArrayList<>();
    private List<Book> filteredBooks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);

        initViews(view);
        loadBooks();
        setupSearch();

        return view;
    }

    private void initViews(View view) {
        etSearchTitle = view.findViewById(R.id.et_search_title);
        etSearchLocation = view.findViewById(R.id.et_search_location);
        btnSearch = view.findViewById(R.id.btn_search);
        searchResultsRecyclerView = view.findViewById(R.id.search_results);

        // 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchResultsRecyclerView.setLayoutManager(layoutManager);

        // 在 FindFragment.java 中修复点击事件
        searchAdapter = new SearchResultsAdapter(filteredBooks, new SearchResultsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                try {
                    // 方法1：使用MainActivity的导航方法
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToBookDetail(book);
                        return;
                    }

                    // 方法2：备用方案
                    if (getContext() == null) return;

                    Intent intent = new Intent(getContext(), BookDetailActivity.class);
                    intent.putExtra("book_id", book.getBookId());
                    intent.putExtra("book_title", book.getTitle());
                    intent.putExtra("book_price", book.getPrice());
                    intent.putExtra("book_location", book.getLocation());

                    // 添加启动标志
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);

                } catch (Exception e) {
                    Log.e("FindFragment", "跳转失败: " + e.getMessage());

                    // 在UI线程显示Toast
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "无法打开详情页，请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void loadBooks() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            allBooks = mainActivity.getAllBooks();
            if (allBooks != null) {
                filteredBooks.clear();
                filteredBooks.addAll(allBooks);
                searchAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setupSearch() {
        // 实时搜索监听（书名）
        etSearchTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performRealTimeSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 实时搜索监听（位置）
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performRealTimeSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 搜索按钮点击
        btnSearch.setOnClickListener(v -> performSearch());

        // 回车键搜索
        etSearchTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performRealTimeSearch() {
        String titleQuery = etSearchTitle.getText().toString().trim();
        String locationQuery = etSearchLocation.getText().toString().trim();

        // 如果两个搜索框都为空，显示所有书籍
        if (titleQuery.isEmpty() && locationQuery.isEmpty()) {
            filteredBooks.clear();
            filteredBooks.addAll(allBooks);
            searchAdapter.notifyDataSetChanged();
            return;
        }

        filterBooksWithPinyin(titleQuery, locationQuery);
    }

    private void performSearch() {
        String titleQuery = etSearchTitle.getText().toString().trim();
        String locationQuery = etSearchLocation.getText().toString().trim();

        if (titleQuery.isEmpty() && locationQuery.isEmpty()) {
            Toast.makeText(getContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            return;
        }

        filterBooksWithPinyin(titleQuery, locationQuery);

        // 显示搜索结果数量
        Toast.makeText(getContext(),
                "找到 " + filteredBooks.size() + " 个结果",
                Toast.LENGTH_SHORT).show();
    }

    private void filterBooksWithPinyin(String titleQuery, String locationQuery) {
        filteredBooks.clear();

        for (Book book : allBooks) {
            boolean matches = true;

            // 书名匹配（支持拼音）
            if (!titleQuery.isEmpty()) {
                matches = matches && PinyinUtils.enhancedMatch(book.getTitle(), titleQuery);
            }

            // 位置匹配（支持拼音）
            if (!locationQuery.isEmpty() && matches) {
                String location = book.getLocation();
                if (location == null) {
                    matches = false;
                } else {
                    matches = matches && PinyinUtils.enhancedMatch(location, locationQuery);
                }
            }

            if (matches) {
                filteredBooks.add(book);
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    // 添加拼音搜索示例提示
    private void showPinyinExamples() {
        // 可以在界面上添加提示信息
        String examples = "拼音搜索示例：\n" +
                "• 'g' 或 'gao' → 高等数学\n" +
                "• 'java' 或 'jb' → Java编程思想\n" +
                "• 'yy' 或 'ying' → 英语四级词汇\n" +
                "• 'sx' 或 'shuxue' → 数学类书籍";

        // 可以显示在界面的提示区域
    }
}