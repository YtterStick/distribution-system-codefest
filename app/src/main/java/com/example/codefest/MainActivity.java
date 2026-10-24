package com.example.codefest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private DistributionAdapter adapter;

    private TextView tvRice, tvCans, tvEmptyHistory;
    private EditText etName, etSize, etAddRice, etAddCans;
    private CheckBox cbPriority;
    private LinearLayout layoutDist, layoutInv, loadingLayout, contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        loadingLayout = findViewById(R.id.loadingLayout);
        contentLayout = findViewById(R.id.contentLayout);
        tvRice = findViewById(R.id.tvRiceStock);
        tvCans = findViewById(R.id.tvCansStock);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        etName = findViewById(R.id.etName);
        etSize = findViewById(R.id.etSize);
        etAddRice = findViewById(R.id.etRestockRice);
        etAddCans = findViewById(R.id.etRestockCans);
        cbPriority = findViewById(R.id.cbPriority);
        layoutDist = findViewById(R.id.layoutDistribution);
        layoutInv = findViewById(R.id.layoutInventory);

        findViewById(R.id.btnDistribute).setOnClickListener(v -> saveRecord());
        findViewById(R.id.btnRestock).setOnClickListener(v -> addStock());

        TabLayout tabs = findViewById(R.id.tabLayout);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutDist.setVisibility(View.VISIBLE);
                    layoutInv.setVisibility(View.GONE);
                } else {
                    layoutDist.setVisibility(View.GONE);
                    layoutInv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        RecyclerView rv = findViewById(R.id.rvHistory);
        adapter = new DistributionAdapter(db.getHistory(), this::confirmDelete);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            updateUI();
        }, 1200);
    }

    private void saveRecord() {
        String name = etName.getText().toString().trim();
        String s = etSize.getText().toString().trim();

        if (name.isEmpty() || s.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int size;
        try {
            size = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid numeric family size", Toast.LENGTH_SHORT).show();
            return;
        }

        if (size <= 0) {
            Toast.makeText(this, "Family size must be at least 1 member", Toast.LENGTH_SHORT).show();
            return;
        }

        int riceNeeded = size * 2;
        int cansNeeded = size * 3;
        boolean isPriority = cbPriority.isChecked();

        int availableRice = db.getStock("rice");
        int availableCans = db.getStock("cans");

        if (availableRice < riceNeeded || availableCans < cansNeeded) {
            String msg = "Insufficient stock!\nNeeded: " + riceNeeded + "kg rice, " + cansNeeded + " cans.\nAvailable: " + availableRice + "kg rice, " + availableCans + " cans.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        long result = db.saveDistribution(name, size, riceNeeded, cansNeeded, isPriority);
        if (result != -1) {
            Toast.makeText(this, "Distribution recorded successfully!", Toast.LENGTH_SHORT).show();
            etName.getText().clear();
            etSize.getText().clear();
            cbPriority.setChecked(false);
            refresh();
        } else {
            Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
        }
    }

    private void addStock() {
        String rStr = etAddRice.getText().toString().trim();
        String cStr = etAddCans.getText().toString().trim();

        if (rStr.isEmpty() && cStr.isEmpty()) {
            Toast.makeText(this, "Please enter stock quantity to add", Toast.LENGTH_SHORT).show();
            return;
        }

        int addRice;
        int addCans;

        try {
            addRice = rStr.isEmpty() ? 0 : Integer.parseInt(rStr);
            addCans = cStr.isEmpty() ? 0 : Integer.parseInt(cStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric quantities", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addRice <= 0 && addCans <= 0) {
            Toast.makeText(this, "Stock quantity must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addRice < 0 || addCans < 0) {
            Toast.makeText(this, "Stock quantities cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addRice > 0) db.updateStock("rice", addRice);
        if (addCans > 0) db.updateStock("cans", addCans);

        Toast.makeText(this, "Stock updated successfully!", Toast.LENGTH_SHORT).show();

        etAddRice.getText().clear();
        etAddCans.getText().clear();
        refresh();
    }

    private void confirmDelete(DistributionRecord r) {
        String msg = String.format(
                getString(R.string.delete_confirm_msg),
                r.getHouseholdName(),
                r.getRiceDistributed(),
                r.getCansDistributed()
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteDistribution(r.getId(), r.getRiceDistributed(), r.getCansDistributed());
                    Toast.makeText(MainActivity.this, "Record deleted and stock restored", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void refresh() {
        adapter.setList(db.getHistory());
        updateUI();
    }

    private void updateUI() {
        int riceStock = db.getStock("rice");
        int cansStock = db.getStock("cans");

        tvRice.setText(getString(R.string.rice_stock_format, riceStock));
        tvCans.setText(getString(R.string.cans_stock_format, cansStock));

        tvEmptyHistory.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
