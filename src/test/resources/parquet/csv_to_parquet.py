import sys
import pandas
import pyarrow as pa
import pyarrow.parquet as pq

if __name__ == "__main__":
    pandas.read_csv(sys.argv[1]).to_parquet(sys.argv[2], engine="pyarrow", compression=None) 
