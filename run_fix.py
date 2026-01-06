import pymysql

# Database connection details
db_config = {
    'host': '118.31.77.102',
    'user': 'root',
    'password': 'Zlw.100525',
    'database': 'contract_system',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def execute_fix():
    print("Connecting to database...")
    try:
        connection = pymysql.connect(**db_config)
        print("Connected.")
        with connection.cursor() as cursor:
            # 1. Update Change Status
            print("Updating change status...")
            sql1 = "UPDATE t_contract_change SET status = 2, approved_at = NOW(), effective_at = NOW() WHERE id = 4 AND status = 1"
            cursor.execute(sql1)
            print(f"Updated {cursor.rowcount} change records.")

            # 2. Update Contract
            print("Updating contract...")
            sql2 = "UPDATE t_contract SET amount = 2.00, version = 'V2', updated_at = NOW() WHERE id = 1 AND amount = 1.00"
            cursor.execute(sql2)
            print(f"Updated {cursor.rowcount} contract records.")
        
        connection.commit()
        print("Commit successful.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    execute_fix()
