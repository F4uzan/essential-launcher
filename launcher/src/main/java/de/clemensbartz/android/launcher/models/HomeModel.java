/*
 * Copyright (C) 2017  Clemens Bartz
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.models;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.clemensbartz.android.launcher.Launcher;
import de.clemensbartz.android.launcher.db.ApplicationUsageDbHelper;
import de.clemensbartz.android.launcher.db.ApplicationUsageModel;

/**
 * Model class for HomeActivity.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public final class HomeModel {

    /** The ID for the grid layout. */
    public static final int GRID_ID = 1;
    /** The ID for the list layout. */
    public static final int LIST_ID = 2;

    /** The total cached number of apps. */
    public static final int NUMBER_OF_APPS = 7;
    /** Constant for descending sorting. */
    private static final String SPACE_DESC = " DESC";
    /** Columns of ApplicationUsage. */
    private static final String[] COLUMNS = {
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME,
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME,
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE,
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED,
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY,
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_HIDDEN
    };
    /** Order by sticky DESC, usage DESC, package name DESC, class name DESC constant. */
    private static final String ORDER_BY =
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY
                    + SPACE_DESC
            + ", "
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE
                    + SPACE_DESC
            + ", "
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME
                    + SPACE_DESC
            + ", "
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME
                    + SPACE_DESC;
    /** Filter for package name and class name constant. */
    private static final String SELECTION =
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME
                    + "=? AND "
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME
                    + "=?";
    /** Where statement for getting only enabled applications. */
    private static final String WHERE =
            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED
                    + "=0 AND ("
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE
                    + ">0 OR "
            + ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY
                    + ">0)";

    /** Database helper. */
    private final SQLiteOpenHelper dbHelper;
    /** Package manager. */
    private final PackageManager pm;
    /** The icLauncher. */
    private final Drawable icLauncher;

    /** Preferences value. */
    private final SharedPreferences preferences;

    /** Cache for most used applications. */
    private final List<ApplicationModel> mostUsedApplications =
            new ArrayList<>(NUMBER_OF_APPS);

    /** Writable SQLiteDatabase. */
    private SQLiteDatabase writableDatabase;

    /** The instance in during application life cycle. */
    private static HomeModel instance;

    /** Key for the appWidgetId property. */
    private static final String KEY_APPWIDGET_ID = "appWidgetId";
    ///** Key for the hide overlay property. */
    //This key has been removed as of version 1.3.
    //private static final String KEY_HIDE_OVERLAY_ID = "hideOverlay";
    /** Key for the appWidgetLayout property. */
    private static final String KEY_APPWIDGET_LAYOUT = "appWidgetLayout";
    /** Key for choosing which layout to display. */
    private static final String KEY_DRAWER_LAYOUT = "drawerLayout";

    /** Value for the appWidgetId property. */
    private int appWidgetId = -1;
    /** Value for the appWidgetLayout property. */
    private int appWidgetLayout = Launcher.WIDGET_LAYOUT_FULL_SCREEN;
    /** Value for the drawerLayout property. */
    private int drawerLayout = 1;

    /**
     *
     * @param activity the Activity
     * @return the instance of the home model.
     */
    public static HomeModel getInstance(final Launcher activity) {
        if (instance == null) {
            instance = new HomeModel(activity);
        }

        return instance;
    }

    /**
     * Create a pair of content values for a package, a package, the usage, if it is disabled
     * or sticky.
     * @param packageName the name of the package
     * @param className the name of the class
     * @param usage the usage
     * @param disabled the disabled state
     * @param sticky the sticky state
     * @param hidden the hidden state
     * @return the content value pair
     */
    private static ContentValues createContentValues(final String packageName, final String className, final Integer usage, final Boolean disabled, final Boolean sticky, final Boolean hidden) {
        final ContentValues values = new ContentValues(5);

        values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME, packageName);
        values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME, className);
        if (sticky != null) {
            values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY, sticky);
        }
        if (disabled != null) {
            values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED, disabled);
        }
        if (usage != null) {
            values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE, usage);
        }
        if (hidden != null) {
            values.put(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_HIDDEN, hidden);
        }

        return values;
    }


    /**
     * Create a new model in a context.
     * @param context the context
     */
    private HomeModel(final Launcher context) {
        preferences = context.getPreferences(Context.MODE_PRIVATE);
        dbHelper = ApplicationUsageDbHelper.getInstance(context);
        pm = context.getApplicationContext().getPackageManager();
        icLauncher = context.getIcLauncher();
    }

    /**
     * Get a database for a mode.
     * @return the database or a readable database for unsupported modes
     */
    private SQLiteDatabase getDatabase() {
        if (writableDatabase == null || !(writableDatabase.isOpen())) {
            writableDatabase = dbHelper.getWritableDatabase();
        }

        return writableDatabase;
    }

    /**
     * Load preference values.
     */
    public void loadValues() {
        updateApplications();

        appWidgetId = preferences.getInt(KEY_APPWIDGET_ID, -1);
        appWidgetLayout = preferences.getInt(KEY_APPWIDGET_LAYOUT, Launcher.WIDGET_LAYOUT_FULL_SCREEN);
        drawerLayout = preferences.getInt(KEY_DRAWER_LAYOUT, 1);
    }

    /**
     *
     * @return the list of most used applications
     */
    public List<ApplicationModel> getMostUsedApplications() {
        return mostUsedApplications;
    }

    /**
     * Convert an object to string.
     * @param object the object
     * @return a string or <code>null</code>, if the object could not be converted to a string
     */
    private String convertToString(final Object object) {
        if (object instanceof String) {
            return (String) object;
        } else {
            return null;
        }
    }

    /**
     * Get the application model.
     * @param contentValue the content values
     * @return an application model, or <code>null</code>, if no application was found
     */
    private ApplicationModel getApplicationModel(final ContentValues contentValue) {
        final String packageName = convertToString(contentValue.get(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME));
        final String className = convertToString(contentValue.get(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME));

        // Report back if one of them is null
        if (packageName == null || className == null) {
            return null;
        }

        final boolean disabled = contentValue.getAsBoolean(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED);
        final boolean sticky = contentValue.getAsBoolean(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY);

        try {
            final ComponentName componentName = new ComponentName(packageName, className);

            final ActivityInfo info = pm.getActivityInfo(componentName, 0);
            if (!info.enabled) {
                return null;
            }
            final ApplicationModel applicationModel = new ApplicationModel();
            applicationModel.packageName = packageName;
            applicationModel.className = className;
            applicationModel.disabled = disabled;
            applicationModel.sticky = sticky;

            final CharSequence label = info.loadLabel(pm);

            if (label != null) {
                applicationModel.label = label.toString();
            } else {
                applicationModel.label = info.name;
            }

            if (applicationModel.label == null) {
                applicationModel.label = "";
            }

            applicationModel.icon = info.loadIcon(pm);

            // Check for when icon can become null (e. g. on Huawei Nexus 6p angler).
            if (applicationModel.icon == null) {
                applicationModel.icon = icLauncher;
            }

            return applicationModel;
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Update the list of applications.
     * <p/>
     * This method has to be called from an async task.
     */
    public void updateApplications() {
        final Map<String, String> applicationsToBeDeleted = new HashMap<>();

        final SQLiteDatabase db = getDatabase();

        mostUsedApplications.clear();

        Cursor c = null;
        try {
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    COLUMNS, WHERE, null, null, null,
                    ORDER_BY, Integer.toString(NUMBER_OF_APPS));

            if (c != null) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    String packageName;
                    String className;
                    ContentValues contentValue;
                    try {
                        packageName = c.getString(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_PACKAGE_NAME));
                        className = c.getString(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_CLASS_NAME));
                        boolean disabled = c.getInt(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED)) > 0;
                        boolean sticky = c.getInt(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY)) > 0;

                        contentValue = createContentValues(packageName, className, null, disabled, sticky, null);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    final ApplicationModel applicationModel = getApplicationModel(contentValue);
                    if (applicationModel == null) {
                        applicationsToBeDeleted.put(packageName, className);
                        break;
                    } else {
                        mostUsedApplications.add(applicationModel);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        // Delete old applications
        for (Map.Entry<String, String> entry : applicationsToBeDeleted.entrySet()) {
            delete(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Toggle the sticky state for an application.
     * @param packageName the package name
     * @param className the class name
     */
    public void toggleSticky(final String packageName, final String className) {
        // Check for deletion
        if (canBeDeleted(packageName, className)) {
            return;
        }

        final SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        Cursor c = null;
        try {
            // Get entry
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    new String[]{
                            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY
                    },
                    SELECTION, new String[]{packageName, className},
                    null, null, null);
            if (c != null) {
                if (c.getCount() > 1) {
                    delete(packageName, className);
                }
                if (c.moveToFirst()) {
                    final boolean sticky = c.getInt(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY)) > 0;
                    // update
                    final ContentValues values = createContentValues(packageName, className, null, null, !sticky, null);

                    db.update(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                            values, SELECTION, new String[]{packageName, className});
                    db.setTransactionSuccessful();
                } else {
                    // insert
                    final ContentValues values = createContentValues(packageName, className, 0, false, true, null);

                    db.insertOrThrow(ApplicationUsageModel.ApplicationUsage.TABLE_NAME, null, values);
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Toggle the disabled state for an application.
     * @param packageName the package name
     * @param className the class name
     */
    public void toggleDisabled(final String packageName, final String className) {
        // Check for deletion
        if (canBeDeleted(packageName, className)) {
            return;
        }

        final SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        Cursor c = null;
        try {
            // Get entry
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    new String[]{
                            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED
                    },
                    SELECTION, new String[]{packageName, className},
                    null, null, null);
            if (c != null) {
                if (c.getCount() > 1) {
                    delete(packageName, className);
                }
                if (c.moveToFirst()) {
                    final boolean disabled = c.getInt(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED)) > 0;
                    // update
                    final ContentValues values = createContentValues(packageName, className, null, !disabled, null, null);

                    db.update(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                            values, SELECTION, new String[]{packageName, className});
                    db.setTransactionSuccessful();
                } else {
                    // insert
                    final ContentValues values = createContentValues(packageName, className, 0, true, false, null);

                    db.insertOrThrow(ApplicationUsageModel.ApplicationUsage.TABLE_NAME, null, values);
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Check if an application is sticky.
     * @param packageName the package name
     * @param className the class name
     * @return if the application is sticky
     */
    public boolean isSticky(final String packageName, final String className) {
        return isField(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_STICKY, packageName, className);
    }

    /**
     * Check if an application is disabled.
     * @param packageName the package name
     * @param className the class name
     * @return if the application is disabled
     */
    public boolean isDisabled(final String packageName, final String className) {
        return isField(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_DISABLED, packageName, className);
    }

    /**
     * Check if value of column is true.
     * @param columnName which column in the table
     * @param packageName the package name
     * @param className the class name
     * @return <code>true</code>, defaults to <code>false</code>
     */
    private boolean isField(final String columnName, final String packageName, final String className) {
        // Check for deletion
        if (canBeDeleted(packageName, className)) {
            return false;
        }

        // Get the database
        final SQLiteDatabase db = getDatabase();

        // Default value is false
        boolean is = false;

        db.beginTransaction();
        Cursor c = null;
        try {
            // Get entry
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    new String[]{
                            columnName
                    },
                    SELECTION, new String[]{packageName, className},
                    null, null, null);
            if (c != null) {
                // Entry could not be found, delete all remaining entries
                if (c.getCount() > 1) {
                    delete(packageName, className);
                }
                // Get first value
                if (c.moveToFirst()) {
                    is = c.getInt(c.getColumnIndexOrThrow(columnName)) > 0;
                }
            }
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }

        return is;
    }

    /**
     * Reset the counter for an application.
     * @param packageName the package name
     * @param className the class name
     */
    public void resetUsage(final String packageName, final String className) {
        final SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        Cursor c = null;
        try {
            // Get entry
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    new String[]{
                            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE
                    },
                    SELECTION, new String[]{packageName, className},
                    null, null, null);
            if (c != null) {
                if (c.getCount() > 1) {
                    delete(packageName, className);
                }
                if (c.moveToFirst()) {
                    // update
                    final ContentValues values = createContentValues(packageName, className, 0, null, null, null);

                    db.update(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                            values, SELECTION, new String[]{packageName, className});
                    db.setTransactionSuccessful();
                } else {
                    // insert
                    final ContentValues values = createContentValues(packageName, className, 0, false, false, null);

                    db.insertOrThrow(ApplicationUsageModel.ApplicationUsage.TABLE_NAME, null, values);
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }

        updateApplications();
    }

    /**
     * Increase the counter of an app.
     * @param packageName the package name
     * @param className the class name
     */
    public void addUsage(final String packageName, final String className) {
        // Check for deletion
        if (canBeDeleted(packageName, className)) {
            return;
        }

        final SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        Cursor c = null;
        try {
            // Get entry
            c = db.query(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                    new String[]{
                            ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE
                    },
                    SELECTION, new String[]{packageName, className},
                    null, null, null);
            if (c != null) {
                if (c.getCount() > 1) {
                    delete(packageName, className);
                }
                if (c.moveToFirst()) {
                    // update
                    final int usage = c.getInt(c.getColumnIndexOrThrow(ApplicationUsageModel.ApplicationUsage.COLUMN_NAME_USAGE));

                    if (usage < Integer.MAX_VALUE) {
                        final ContentValues values = createContentValues(packageName, className, usage + 1, null, null, null);

                        db.update(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                                values, SELECTION, new String[]{packageName, className});
                        db.setTransactionSuccessful();
                    } else {
                        resetUsage(packageName, className);
                    }
                } else {
                    // insert
                    final ContentValues values = createContentValues(packageName, className, 1, false, false, null);

                    db.insertOrThrow(ApplicationUsageModel.ApplicationUsage.TABLE_NAME, null, values);
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }

        updateApplications();
    }

    /**
     * Check if an app can be deleted.
     * @param packageName the package name
     * @param className the class name
     * @return true if it can, otherwise false
     */
    private boolean canBeDeleted(final String packageName, final String className) {
        if (packageName == null || className == null) {
            delete(packageName, className);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Delete all entries for packageName and className.
     * @param packageName the package name
     * @param className the class name
     */
    private void delete(final String packageName, final String className) {
        final SQLiteDatabase db = getDatabase();
        db.delete(ApplicationUsageModel.ApplicationUsage.TABLE_NAME,
                SELECTION, new String[]{packageName, className});
    }

    /**
     *
     * @return the id of the app widget
     */
    public int getAppWidgetId() {
        return appWidgetId;
    }

    /**
     * Set the currently id of the app widget.
     * @param appWidgetId the id of the app widget
     */
    public void setAppWidgetId(final int appWidgetId) {
        preferences.edit().putInt(KEY_APPWIDGET_ID, appWidgetId).apply();
        this.appWidgetId = appWidgetId;
    }

    /**
     *
     * @return the current layout status
     */
    public int getAppWidgetLayout() {
        return appWidgetLayout;
    }

    /**
     * Set the layout for the app widget.
     * @param appWidgetLayout the layout for the app widget
     */
    public void setKeyAppwidgetLayout(final int appWidgetLayout) {
        preferences.edit().putInt(KEY_APPWIDGET_LAYOUT, appWidgetLayout).apply();
        this.appWidgetLayout = appWidgetLayout;
    }

    /**
     *
     * @return the current drawer layout
     */
    public int getDrawerLayout() {
        return drawerLayout;
    }

    /**
     * Set the drawer layout.
     * @param drawerLayout the new layout
     */
    public void setDrawerLayout(final int drawerLayout) {
        preferences.edit().putInt(KEY_DRAWER_LAYOUT, drawerLayout).apply();
        this.drawerLayout = drawerLayout;
    }
}
