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

def default_converter(o):
    if isinstance(o, datetime.datetime):
        return o.__str__()

def debug_workflow():
    conn = pymysql.connect(**db_config)
    result = {}
    try:
        with conn.cursor() as cursor:
            # Get User ID for xxy
            cursor.execute("SELECT id FROM sys_user WHERE username = 'xxy'")
            user = cursor.fetchone()
            if not user:
                result['error'] = "User xxy not found"
                return
            
            # Change
            cursor.execute("SELECT * FROM t_contract_change WHERE initiator_id = %s ORDER BY created_at DESC LIMIT 1", (user['id'],))
            change = cursor.fetchone()
            result['change'] = change
            
            if change:
                # Instance
                cursor.execute("SELECT * FROM wf_instance WHERE contract_id = %s AND remark = 'CONTRACT_CHANGE'", (change['id'],))
                instance = cursor.fetchone()
                result['instance'] = instance
                
                if instance:
                    # Tasks
                    cursor.execute("""
                        SELECT t.*, u.username, u.real_name, u.role, u.primary_role, u.dept_id
                        FROM wf_task t
                        LEFT JOIN sys_user u ON t.assignee_id = u.id
                        WHERE t.instance_id = %s
                        ORDER BY t.create_time ASC
                    """, (instance['id'],))
                    result['tasks'] = cursor.fetchall()
                    
                    # Scenario Nodes
                    cursor.execute("SELECT * FROM wf_scenario_node WHERE scenario_id = %s ORDER BY node_order ASC", (instance['scenario_id'],))
                    result['nodes'] = cursor.fetchall()

    finally:
        conn.close()
        
    with open('debug_output.json', 'w', encoding='utf-8') as f:
        json.dump(result, f, default=default_converter, indent=2, ensure_ascii=False)

if __name__ == "__main__":
    debug_workflow()
