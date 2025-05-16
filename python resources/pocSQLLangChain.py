from langchain_community.utilities import SQLDatabase
from langchain_core.prompts import ChatPromptTemplate
from langchain_ollama.llms import OllamaLLM
from langchain_community.tools.sql_database.tool import QuerySQLDatabaseTool
import sys

# Connect to PostgreSQL
db = SQLDatabase.from_uri("postgresql+psycopg2://adempiere:adempiere@localhost:5432/idempiere",
    schema='adempiere',
    include_tables=["v_product_info","v_order_info","v_orderline_info","v_bpartner_info"],
    view_support = True,)

llm = OllamaLLM(model="llama3.1:8b")
    
from typing_extensions import TypedDict
class State(TypedDict):
    question: str
    query: str
    result: str
    answer: str
    
prompt_template = """
system
Given an input question, create a syntactically correct PostgreSQL query to run to help find the answer. Unless the user specifies in his question a specific number of examples they wish to obtain, always limit your query to at most 5 results. You can order the results by a relevant column to return the most interesting examples in the database.

Never query for all the columns from a specific table, only ask for the few relevant columns given the question.

Pay attention to use only the column names that you can see in the schema description. Be careful to not query for columns that do not exist. Also, pay attention to which column is in which table.

Only use the following tables:
{table_info}

Return only the SQL query. Do not include explanations, markdown, code formatting, or any extra text. Your entire response must be a valid SQL statement only.
The ID columns start always with the table name and end with the _id suffix.

Below are a number of examples of questions and their corresponding SQL queries.
{examples}

human
Question: {input}
"""

examples = [
    {"input": "List all orders.", "query": "SELECT * FROM v_order_info;"},
    {
        "input": "Find all sales orders.",
        "query": "SELECT * FROM v_order_info WHERE issotrx = 'Y';",
    },
    {
        "input": "List all processed orders",
        "query": "SELECT * FROM v_order_info WHERE DocStatus IN ('CO','CL');",
    },
    {
        "input": "Find the total sold",
        "query": "SELECT SUM(grandtotal) FROM v_order_info WHERE DocStatus NOT IN ('RE',VO') AND issotrx = 'Y';",
    },
    {
        "input": "List all products that were sold",
        "query": "SELECT v_product_info.name FROM v_product_info JOIN v_orderline_info ON v_product_info.m_product_id = v_orderline_info.m_product_id JOIN v_order_info ON v_order_info.c_order_id = v_orderline_info.c_order_id WHERE v_order_info.DocStatus NOT IN ('RE','VO') AND v_order_info.issotrx = 'Y';",
    },
    {
        "input": "How many customer do we have",
        "query": "SELECT COUNT(*) FROM v_bpartner_info WHERE iscustomer = 'Y';",
    },
    {
        "input": "Find the total number of purchase orders.",
        "query": "SELECT COUNT(*) FROM v_order_info WHERE issotrx = 'N';",
    },
    {
        "input": "List the top 5 customers",
        "query": "SELECT bp.name AS customer_name,SUM(o.grandtotal) AS total_purchased FROM v_bpartner_info bp JOIN v_order_info o ON o.c_bpartner_id = bp.c_bpartner_id WHERE bp.iscustomer = 'Y' AND o.issotrx = 'Y' GROUP BY bp.name ORDER BY total_purchased DESC LIMIT 5;",
    },
    {
        "input": "What are the top 5 sold products",
        "query": "SELECT p.name AS product_name,SUM(ol.qtyentered) AS total_quantity_sold FROM v_orderline_info ol JOIN v_product_info p ON ol.m_product_id = p.m_product_id GROUP BY p.name ORDER BY total_quantity_sold DESC LIMIT 5;",
    },
    {
        "input": "Which orders were placed in 2000",
        "query": "SELECT * FROM v_order_info WHERE EXTRACT(YEAR FROM dateordered) = '2002';",
    },
    {
        "input": "How many products can be sold",
        "query": "SELECT COUNT(*) FROM v_product_info WHERE issold = 'Y';",
    },
]
    
def write_query(state: State):
    """Generate SQL query to fetch information. Syntactically valid SQL query."""
    prompt = prompt_template.format(
        table_info=db.get_table_info(),
        input=state["question"],
        examples="\n".join(
    f"Question: {example['input']}\nSQL Query: {example['query']}" for example in examples
        ),
    )
    result = llm.invoke(prompt)
    return {"query": result}

question = sys.argv[1]  # Receive the question from Java

sql_query = write_query({"question": question})

def execute_query(state: State):
    """Execute SQL query."""
    execute_query_tool = QuerySQLDatabaseTool(db=db)
    return {"result": execute_query_tool.invoke(state["query"])}
    
query_result = execute_query({"query": sql_query})

def generate_answer(state: State):
    """Answer question using retrieved information as context, formatted in HTML with <br> tags for line breaks."""
    prompt = f"""
            You are a helpful assistant.

            You will be given a question, the SQL query used, and the result of that query.

            Your job is to return ONLY the final answer in **HTML**, using <br> to separate lines. Do not include SQL, explanations, or markdown. Output only a valid HTML string with the answer.

            Here is the user's question:
            {state['question']}

            Here is the SQL query that was executed:
            {state['query']}

            And here is the result of that query:
            {state['result']}

            Using this information, write a clear, natural language answer for the user. Be concise and only include the answer — do not rephrase the question.
            """
    response = llm.invoke(prompt)
    return response;
    
all_result = generate_answer({"query": sql_query, "question": question, "result": query_result})
print(all_result)
