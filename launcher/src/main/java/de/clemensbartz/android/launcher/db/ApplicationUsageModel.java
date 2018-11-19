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

package de.clemensbartz.android.launcher.db;

import android.provider.BaseColumns;

/**
 * Utility class for the application usage model.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public final class ApplicationUsageModel {
    /** A comma. */
    private static final String COMMA = ",";
    /** The create SQL String. */
    static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS "
            + ApplicationUsage.TABLE_NAME
            + " ("
                    + ApplicationUsage.COLUMN_NAME_PACKAGE_NAME + " "
                    + ApplicationUsage.COLUMN_TYPE_PACKAGE_NAME
                    + COMMA + " "
                    + ApplicationUsage.COLUMN_NAME_CLASS_NAME + " "
                    + ApplicationUsage.COLUMN_TYPE_CLASS_NAME
                    + COMMA + " "
                    + ApplicationUsage.COLUMN_NAME_USAGE + " "
                    + ApplicationUsage.COLUMN_TYPE_USAGE
                    + COMMA + " "
                    + ApplicationUsage.COLUMN_NAME_DISABLED + " "
                    + ApplicationUsage.COLUMN_TYPE_DISABLED
                    + COMMA + " "
                    + ApplicationUsage.COLUMN_NAME_STICKY + " "
                    + ApplicationUsage.COLUMN_TYPE_STICKY
                    + COMMA + " "
                    + ApplicationUsage.COLUMN_NAME_HIDDEN + " "
                    + ApplicationUsage.COLUMN_TYPE_HIDDEN
            + ")";
    /** The DDM update for Version 3 of the database. */
    static final String ALTER_TABLE_3 = "ALTER TABLE "
                    + ApplicationUsage.TABLE_NAME
                    + " ADD COLUMN "
                    + ApplicationUsage.COLUMN_NAME_HIDDEN + " "
                    + ApplicationUsage.COLUMN_TYPE_HIDDEN;
    /** The content update for Version 3 of the database. */
    static final String UPDATE_CONTENT_3 = "UPDATE "
                    + ApplicationUsage.TABLE_NAME
                    + " SET "
                    + ApplicationUsage.COLUMN_NAME_HIDDEN
                    + "=0";
    /** The drop SQL String. */
    static final String DROP_SQL =
            "DROP TABLE "
            + ApplicationUsage.TABLE_NAME;

    /**
     * Hidden constructor.
     */
    private ApplicationUsageModel() {

    }

    /**
     * Class for defining the table ApplicationUsage.
     */
    public static class ApplicationUsage implements BaseColumns {
        /** Table name. */
        public static final String TABLE_NAME = "ApplicationUsage";
        /** Name of the column for package name. */
        public static final String COLUMN_NAME_PACKAGE_NAME = "packagename";
        /** Type of the column for package name. */
        private static final String COLUMN_TYPE_PACKAGE_NAME = "TEXT";
        /** Name of the column for class name. */
        public static final String COLUMN_NAME_CLASS_NAME = "classname";
        /** Type of the column for class name. */
        private static final String COLUMN_TYPE_CLASS_NAME = "TEXT";
        /** Name of the column for usage. */
        public static final String COLUMN_NAME_USAGE = "usage";
        /** Type of the column for usage. */
        private static final String COLUMN_TYPE_USAGE = "INTEGER";
        /** Name of the column for disabled. */
        public static final String COLUMN_NAME_DISABLED = "disabled";
        /** Type of the column for disabled. */
        private static final String COLUMN_TYPE_DISABLED = "BOOLEAN";
        /** Name of the column for sticky. */
        public static final String COLUMN_NAME_STICKY = "sticky";
        /** Type of the column for sticky. */
        @SuppressWarnings("WeakerAccess")
        public static final String COLUMN_TYPE_STICKY = "BOOLEAN";
        /** Name of the column for hidden. */
        public static final String COLUMN_NAME_HIDDEN = "hidden";
        /** Type of the column for hidden. */
        public static final String COLUMN_TYPE_HIDDEN = "BOOLEAN";
    }
}
