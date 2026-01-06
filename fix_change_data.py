import pymysql
import json
import datetime

db_config = {
    'host': '118.31.77.102',
    'user': 'root',
    'password': 'Zlw.100525',
    'database': 'contract_system',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def fix_change_data():
    conn = pymysql.connect(**db_config)
    try:
        with conn.cursor() as cursor:
            # 1. Get the stuck change (ID 5)
            change_id = 5
            cursor.execute("SELECT * FROM t_contract_change WHERE id = %s", (change_id,))
            change = cursor.fetchone()
            
            if not change:
                print("Change 5 not found!")
                return
            
            if change['status'] == 2:
                print("Change 5 already approved.")
            else:
                print(f"Updating Change 5 status from {change['status']} to 2 (Approved)")
                cursor.execute("UPDATE t_contract_change SET status = 2, approved_at = NOW(), effective_at = NOW() WHERE id = %s", (change_id,))
            
            # 2. Apply comparison to contract
            contract_id = change['contract_id']
            print(f"Applying change to Contract {contract_id}")
            
            # Parse diff_data
            diff_data = json.loads(change['diff_data'])
            if 'afterContent' in diff_data:
                after = diff_data['afterContent']
                new_amount = after.get('amount')
                new_content = after.get('content')
                new_name = after.get('name')
                
                print(f"New Amount: {new_amount}, New Name: {new_name}")
                
                # Update contract
                cursor.execute("""
                    UPDATE t_contract 
                    SET amount = %s, name = %s, content = %s, updated_at = NOW(), version = 'V2'
                    WHERE id = %s
                """, (new_amount, new_name, new_content, contract_id))
                
                print(f"Contract {contract_id} updated successfully.")
            else:
                print("No afterContent in diff_data")

            conn.commit()
            print("Fix applied successfully.")

    except Exception as e:
        print(f"Error: {e}")
        conn.rollback()
    finally:
        conn.close()

if __name__ == "__main__":
    fix_change_data()
