import pymysql
import sys

def get_change_status(change_no):
    try:
        # Connect to the remote database
        connection = pymysql.connect(
            host='118.31.77.102',
            user='root',
            password='Zlw.100525',
            database='contract_system',
            charset='utf8mb4',
            cursorclass=pymysql.cursors.DictCursor
        )

        with connection:
            with connection.cursor() as cursor:
                # 1. Find the change request ID
                sql_change = "SELECT id, contract_id, amount_diff FROM t_contract_change WHERE change_no = %s"
                cursor.execute(sql_change, (change_no,))
                change = cursor.fetchone()
                
                if not change:
                    print(f"Error: Change request {change_no} not found.")
                    return

                print(f"Found Change Request: ID={change['id']}, Diff={change['amount_diff']}")

                # 2. Find the Workflow Instance
                sql_instance = "SELECT id, scenario_id, current_node_order, status FROM wf_instance WHERE contract_id = %s AND remark = 'CONTRACT_CHANGE' ORDER BY id DESC LIMIT 1"
                cursor.execute(sql_instance, (change['id'],))
                instance = cursor.fetchone()

                if not instance:
                    print("Error: No workflow instance found for this change.")
                    return

                print(f"Workflow Instance: ID={instance['id']}, Scenario={instance['scenario_id']}, Node={instance['current_node_order']}, Status={instance['status']}")
                print("-" * 30)

                # 3. List All Tasks (History)
                sql_history = "SELECT id, node_id, assignee_id, status, create_time, finish_time FROM wf_task WHERE instance_id = %s ORDER BY id ASC"
                cursor.execute(sql_history, (instance['id'],))
                tasks = cursor.fetchall()
                
                print("Task History:")
                for t in tasks:
                    # Find assignee name
                    cursor.execute("SELECT real_name, role FROM sys_user WHERE id = %s", (t['assignee_id'],))
                    u = cursor.fetchone()
                    uname = u['real_name'] if u else "Unknown"
                    urole = u['role'] if u else "N/A"
                    
                    status_map = {0: 'PENDING', 1: 'APPROVED', 2: 'REJECTED'}
                    st = status_map.get(t['status'], str(t['status']))
                    print(f"  - [Task {t['id']}] Assignee: {uname}({urole}) | Status: {st} | Created: {t['create_time']}")

                print("-" * 30)
                
                # Check for pending tasks again to be sure
                pending = [t for t in tasks if t['status'] == 0]
                if not pending:
                    if instance['status'] == 1: # Running
                        print("⚠️ WARNING: Instance is RUNNING but has NO PENDING TASKS.")
                        print("Possible causes:")
                        print("  1. System failed to find the next approver (Node configuration issue).")
                        print("  2. Database transaction issue.")
                    elif instance['status'] == 2: # Completed
                        print("✅ Workflow Instance is COMPLETED.")
                        if change['id']: # Check change status again
                             cursor.execute("SELECT status FROM t_contract_change WHERE id = %s", (change['id'],))
                             final_change = cursor.fetchone()
                             print(f"Contract Change Status: {final_change['status']} (0=Draft, 1=Approving, 2=Passed)")
                             if final_change['status'] == 1:
                                 print("❌ BUG CONFIRMED: Workflow finished but Change Status is not updated!")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    get_change_status("BG-20260107-4003")
