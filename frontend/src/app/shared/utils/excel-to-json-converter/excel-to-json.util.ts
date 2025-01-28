import * as XLSX from 'xlsx';

export interface ExcelRow {
  [key: string]: any;
}

export interface ExcelToJsonResult {
  data: ExcelRow[];
  errors: string[];
}

export class ExcelToJsonConverter {
  /**
   * Converts an Excel file to JSON.
   * @param file The Excel file to parse.
   * @returns Parsed data and any errors encountered.
   */
  static convert(file: File): Promise<ExcelToJsonResult> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        try {
          const binaryData = e.target?.result;
          if (!binaryData) {
            reject('File could not be read.');
            return;
          }

          const workbook = XLSX.read(binaryData, { type: 'binary' });
          const sheetName = workbook.SheetNames[0];
          const sheet = workbook.Sheets[sheetName];
          const data = XLSX.utils.sheet_to_json(sheet) as ExcelRow[];

          resolve({ data, errors: [] });
        } catch (error) {
          reject(`Failed to parse Excel file: ${error}`);
        }
      };

      reader.onerror = () => reject('Error reading the file.');
      reader.readAsBinaryString(file);
    });
  }
}
