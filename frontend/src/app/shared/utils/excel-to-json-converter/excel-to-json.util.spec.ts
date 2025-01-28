import { ExcelToJsonConverter } from './excel-to-json.util';

describe('ExcelToJsonConverter', () => {
  it('should parse a valid Excel file', async () => {
    const mockFile = new File(['mock data'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const result = await ExcelToJsonConverter.convert(mockFile);
    expect(result.data).toBeDefined();
    expect(result.errors).toEqual([]);
  });

  it('should return an error for an invalid file', async () => {
    const mockFile = new File(['invalid data'], 'test.txt', { type: 'text/plain' });
    await expectAsync(ExcelToJsonConverter.convert(mockFile)).toBeRejectedWith('Failed to parse Excel file');
  });
});
