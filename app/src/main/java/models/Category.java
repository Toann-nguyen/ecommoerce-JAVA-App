package models;

public class Category {

        private String id;
        private String name;
        private String iconUrl;

        // Constructor mặc định cần thiết cho Firebase
        public Category() {}

        public Category(String id, String name, String iconUrl) {
            this.id = id;
            this.name = name;
            this.iconUrl = iconUrl;
        }

        // Getters và Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    }

