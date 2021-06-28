package com.example.waterami;

import android.provider.BaseColumns;

public class tabelas {
    private tabelas(){};

    public static class info  implements BaseColumns {
        public static final String TABLE_NAME = "info";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String COLUMN_NAME_SECURITY_LEVEL = "security_level";
        public static final String COLUMN_NAME_USER_GROUP = "user_group";

    }
    public static class tca implements BaseColumns {
        public static final String TABLE_NAME = "tca";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
    }


    public static class medidas implements BaseColumns {
        public static final String TABLE_NAME = "medidas";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_MEDIDA = "medida";
    }

    public static class tca_id implements BaseColumns {
        public static final String TABLE_NAME = "tca_id";
        public static final String COLUMN_NAME_ID = "id";

    }



}
