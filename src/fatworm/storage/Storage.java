package fatworm.storage;

import fatworm.storagemanager.*;
import fatworm.record.RecordFile;
import fatworm.record.Schema;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class Storage implements StorageManagerInterface {
    private Database current = null, tempDB = null;
    private static String tempDBName = "TEMP";
    private String currentName = "";
    private Map<String, Database> map = new HashMap<String, Database>();
    private String path = "test" + java.io.File.separator;

    private int tempCount = 0;

    private static Storage instance = null;

    private Storage() {
    }

    private Database tempDB() {
        if (tempDB != null)
            return tempDB;
        else {
            File f = new File(fileName(tempDBName, false));

            try {
                if (f.exists())
                    f.delete();
                tempDB = new Database(fileName(tempDBName, false));
                return tempDB;
            } catch (java.io.IOException e) {
                return null;
            }
        }
    }

    public static Storage getInstance() {
        if (instance == null)
            instance = new Storage();
        return instance;
    }

    private String fileName(String dbName) {
        return fileName(dbName, true);
    }

    private String fileName(String dbName, boolean suffix) {
        if (suffix)
            return path + dbName + ".db";
        else
            return path + dbName;
    }

    public boolean createDatabase(String name) {
        File f = new File(fileName(name));
        if (f.exists())
            return false;
        else {
            try {
                Database db = new Database(fileName(name));
                map.put(name, db);
                return true;
            } catch (java.io.FileNotFoundException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }
        }
    }

	public boolean useDatabase(String name) {
        File f = new File(fileName(name));
        if (!f.exists())
            return false;

        Database db = map.get(name);
        if (db == null) {
            try {
                db = new Database(fileName(name));
                map.put(name, db);
            } catch (java.io.IOException e) {
                return false;
            }
        }

        current = db;
        currentName = name;

        return true;
    }

    public boolean dropDatabase(String name) {
        File file = new File(fileName(name));
        if (!file.exists())
            return false;

	Database db = map.get(name);
	if (db != null) {
	    db.close();
	    map.remove(name);
	}
        if (!file.delete())
            return false;

        if (current != null && currentName.equals(name))
            current = null;

        return true;
    }

    public RecordFile getTable(String tablename) {
        try {
            if (current == null)
                return null;
            else
                return current.getTable(tablename);
        } catch (java.io.IOException e) {
            return null;
        }
    }

	public RecordFile insertTable(String tablename, Schema schema) {
        if (current != null) {
            try {
                return current.insertTable(tablename, schema);
            } catch (java.io.IOException e) {
                return null;
            }
        } else
            return null;
    }

    public RecordFile insertTempTable() {
        Table table;
        try {
            table = tempDB.insertTable("t" + tempCount, null);
        } catch (java.io.IOException e) {
            return null;
        }
        ++tempCount;
        return table;
    }

	public void dropTable(String name) {
        if (current != null) {
            try {
                current.dropTable(name);
            } catch (java.io.IOException e) {
            }
        }
    }

    public void save() {
        if (current != null) {
            try {
                current.save();
            } catch (java.io.IOException e) {
            }
        }
    }

    public void setPath(String path) {
        if (!path.endsWith(java.io.File.separator))
            path += java.io.File.separator;
        this.path = path;
    }
}
