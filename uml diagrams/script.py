#!/usr/bin/env python3
"""
Generate Graphviz visualization of SmartFinance project structure
"""

from graphviz import Digraph

def create_project_graph():
    """Create a directed graph of the project structure"""
    
    # Create a new directed graph
    dot = Digraph(comment='SmartFinance Project Structure')
    dot.attr(rankdir='LR', size='12,10')
    dot.attr('node', shape='box', style='rounded,filled', fillcolor='lightblue')
    
    # Root
    dot.node('src', 'src', fillcolor='lightgray')
    dot.node('main', 'main', fillcolor='lightgray')
    dot.node('java', 'java', fillcolor='lightgray')
    dot.node('smartfinance', 'com.smartfinance', fillcolor='lightyellow')
    
    # Main structure
    dot.edge('src', 'main')
    dot.edge('main', 'java')
    dot.edge('java', 'smartfinance')
    
    # Controller package
    dot.node('controller', 'Controller', fillcolor='lightgreen')
    dot.edge('smartfinance', 'controller')
    
    # Admin controllers
    dot.node('admin_pkg', 'Admin', fillcolor='wheat')
    dot.edge('controller', 'admin_pkg')
    
    admin_controllers = [
        'AdminController',
        'AdminMenuController',
        'ClientCellController',
        'ClientsController',
        'CreateClientController',
        'depositController',
        'reportAdminController'
    ]
    
    for ctrl in admin_controllers:
        dot.node(f'admin_{ctrl}', ctrl, fillcolor='#FFE5B4')
        dot.edge('admin_pkg', f'admin_{ctrl}')
    
    # Client controllers
    dot.node('client_pkg', 'Client', fillcolor='wheat')
    dot.edge('controller', 'client_pkg')
    
    client_controllers = [
        'accountController',
        'BudgetController',
        'CategoryController',
        'ClientController',
        'ClientMenuController',
        'DashboardController',
        'InvestmentController',
        'profileController',
        'reportController',
        'TransactionCellController',
        'TransactionController',
        'LoginController'
    ]
    
    for ctrl in client_controllers:
        dot.node(f'client_{ctrl}', ctrl, fillcolor='#FFE5B4')
        dot.edge('client_pkg', f'client_{ctrl}')
    
    # Models package
    dot.node('models', 'Models', fillcolor='lightcoral')
    dot.edge('smartfinance', 'models')
    
    models = [
        'Account',
        'Admin',
        'Budget',
        'Client',
        'ClientReport',
        'DatabaseDriver',
        'GeneratedReport',
        'Investment',
        'Model',
        'RiskProfile',
        'SavingsAccount',
        'Transaction',
        'WalletAccount'
    ]
    
    for model in models:
        fillcolor = '#FFA07A' if model == 'RiskProfile' else '#FFB6C1'
        dot.node(f'model_{model}', model, fillcolor=fillcolor)
        dot.edge('models', f'model_{model}')
    
    # Service package
    dot.node('service', 'service', fillcolor='lightseagreen')
    dot.edge('smartfinance', 'service')
    
    services = [
        'APIClient',
        'InvestmentService'
    ]
    
    for svc in services:
        dot.node(f'service_{svc}', svc, fillcolor='#20B2AA')
        dot.edge('service', f'service_{svc}')
    
    # Views package
    dot.node('views', 'Views', fillcolor='plum')
    dot.edge('smartfinance', 'views')
    
    views = [
        'AccountType',
        'AdminMenuOption',
        'CategoryTransaction',
        'ClientCellFactory',
        'ClientMenuOptions',
        'TransactionCellFactory',
        'ViewFactory'
    ]
    
    for view in views:
        fillcolor = '#DDA0DD' if 'Factory' in view else '#E6E6FA'
        dot.node(f'view_{view}', view, fillcolor=fillcolor)
        dot.edge('views', f'view_{view}')
    
    # App
    dot.node('app', 'App', fillcolor='gold', shape='doubleoctagon')
    dot.edge('smartfinance', 'app')
    
    return dot

def main():
    """Main function to generate and save the graph"""
    
    # Create the graph
    graph = create_project_graph()
    
    # Render the graph
    output_format = 'png'  # Can be changed to 'pdf', 'svg', etc.
    output_file = 'smartfinance_structure'
    
    graph.render(output_file, format=output_format, cleanup=True)
    print(f"Graph generated successfully: {output_file}.{output_format}")
    
    # Also save the DOT source file
    graph.save(f'{output_file}.dot')
    print(f"DOT source saved: {output_file}.dot")

if __name__ == '__main__':
    main()