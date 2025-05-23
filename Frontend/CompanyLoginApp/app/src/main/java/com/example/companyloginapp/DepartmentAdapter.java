package com.example.companyloginapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;
import java.util.List;

public class DepartmentAdapter extends BaseAdapter {

    private final Context context;
    private final List<JSONObject> departments;
    private final DepartmentDeleteCallback deleteCallback;
    private final DepartmentClickCallback clickCallback;

    public interface DepartmentDeleteCallback {
        void onDeleteClicked(int departmentId);
    }

    public interface DepartmentClickCallback {
        void onDepartmentClicked(int departmentId, String departmentName);
    }

    public DepartmentAdapter(Context context,
                             List<JSONObject> departments,
                             DepartmentDeleteCallback deleteCallback,
                             DepartmentClickCallback clickCallback) {
        this.context = context;
        this.departments = departments;
        this.deleteCallback = deleteCallback;
        this.clickCallback = clickCallback;
    }

    @Override
    public int getCount() {
        return departments.size();
    }

    @Override
    public Object getItem(int position) {
        return departments.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return departments.get(position).getInt("id");
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject dept = departments.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_department, parent, false);
        }

        TextView nameView = convertView.findViewById(R.id.department_name);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        try {
            int departmentId = dept.getInt("id");
            String name = dept.getString("departmentName");

            nameView.setText(name);
            deleteButton.setOnClickListener(v -> deleteCallback.onDeleteClicked(departmentId));
            convertView.setOnClickListener(v -> clickCallback.onDepartmentClicked(departmentId, name));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
