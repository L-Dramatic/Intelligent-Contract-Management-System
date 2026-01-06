import pymysql

db_config = {
    'host': '118.31.77.102',
    'user': 'root',
    'password': 'Zlw.100525',
    'database': 'contract_system',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def add_node():
    conn = pymysql.connect(**db_config)
    try:
        with conn.cursor() as cursor:
            # Check if node already exists
            cursor.execute("SELECT * FROM wf_scenario_node WHERE scenario_id = 'A2-Tier1' AND role_code = 'DEPT_MANAGER'")
            node = cursor.fetchone()
            if node:
                print("DEPT_MANAGER node already exists for A2-Tier1")
                return

            print("Adding DEPT_MANAGER node to A2-Tier1...")
            # Insert new node as Order 3
            sql = """
                INSERT INTO wf_scenario_node 
                (scenario_id, node_order, role_code, node_level, action_type, is_mandatory, can_skip, created_at)
                VALUES 
                ('A2-Tier1', 3, 'DEPT_MANAGER', 'CITY', 'APPROVE', 1, 0, NOW())
            """
            cursor.execute(sql)
            print("Node added successfully.")
            conn.commit()
            
    finally:
        conn.close()

if __name__ == "__main__":
    add_node()
