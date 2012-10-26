package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TableRowColSpanConverterTest extends TestCase {

	TableRowColSpanConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TableRowColSpanConverter();
		PropertyConfigurator.configure("log4j.properties");
	}


	public void testConvertRowAndColSpans() {
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Head 1  </p></th>\n" + 
				"<th><p> Head 2 </p></th>\n" + 
				"<th><p> Head 3 </p></th>\n" + 
				"<th><p> Head 4 </p></th>\n" + 
				"<th><p> Head 5 </p></th>\n" + 
				"<th><p> Head 6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Row 1 </p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Header </p></th>\n" + 
				"<td><p> Colspan Here ::UWCTOKENCOLSPANS:5::</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Head 1  </p></th>\n" + 
				"<th><p> Head 2 </p></th>\n" + 
				"<th><p> Head 3 </p></th>\n" + 
				"<th><p> Head 4 </p></th>\n" + 
				"<th><p> Head 5 </p></th>\n" + 
				"<th><p> Head 6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Row 1 </p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" +
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" +
				"<th><p> :::          </p></th>\n" +
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" +
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Header </p></th>\n" + 
				"<td colspan='5'><p> Colspan Here </p></td>\n" +
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		actual = tester.convertColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertRowSpans() {
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Head 1  </p></th>\n" + 
				"<th><p> Head 2 </p></th>\n" + 
				"<th><p> Head 3 </p></th>\n" + 
				"<th><p> Head 4 </p></th>\n" + 
				"<th><p> Head 5 </p></th>\n" + 
				"<th><p> Head 6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Row 1 ::UWCTOKENROWSPANS:4::</p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Head 1  </p></th>\n" + 
				"<th><p> Head 2 </p></th>\n" + 
				"<th><p> Head 3 </p></th>\n" + 
				"<th><p> Head 4 </p></th>\n" + 
				"<th><p> Head 5 </p></th>\n" + 
				"<th><p> Head 6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th rowspan='4'><p> Row 1 </p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n\n" + 
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		actual = tester.convertRowspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testComplicated() { //multiple row and col spans?
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td><p> some colspan (note the double pipe) ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td><p> this cell spans vertically ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 8 some colspan (note the double pipe) ::UWCTOKENCOLSPANS:3::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 9 Col 1    ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 10 Col 2        ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 10 Col 2        ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td colspan='2'><p> some colspan (note the double pipe) </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td rowspan='3'><p> this cell spans vertically </p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td colspan='3'><p> Row 8 some colspan (note the double pipe) </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='3'><p> Row 9 Col 1    </p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td colspan='2'><p> Row 10 Col 2        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td colspan='2'><p> Row 10 Col 2        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n";

		Page page =new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSameAsComplicated_NoColspans() { 
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1</p></td>\n" + 
				"<td><p> Row 2 Col 2</p></td>\n" +
				"<td><p> Row 2 Col 3</p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td><p> this cell spans vertically ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 8 Col 1</p></td>\n" +
				"<td><p> Row 8 Col 2</p></td>\n" +
				"<td><p> Row 8 Col 3</p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 9 Col 1    ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 10 Col 2</p></td>\n" +
				"<td><p> Row 10 Col 3</p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 11 Col 2</p></td>\n" +
				"<td><p> Row 11 Col 3</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1</p></td>\n" + 
				"<td><p> Row 2 Col 2</p></td>\n" +
				"<td><p> Row 2 Col 3</p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td rowspan='3'><p> this cell spans vertically </p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 8 Col 1</p></td>\n" +
				"<td><p> Row 8 Col 2</p></td>\n" +
				"<td><p> Row 8 Col 3</p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='3'><p> Row 9 Col 1    </p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td><p> Row 10 Col 2</p></td>\n" +
				"<td><p> Row 10 Col 3</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td><p> Row 11 Col 2</p></td>\n" +
				"<td><p> Row 11 Col 3</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n";

		Page page =new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testComplicated_SameRow() { //multiple row and col spans?
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" +
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     ::UWCTOKENROWSPANS:4::</p></td>\n" + 
				"<td><p> Row 1 Col 3        ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"</tr>\n" +
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> :::        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> :::        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td><p> :::     </p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td rowspan='4'><p> Row 1 Col 2     </p></td>\n" + 
				"<td rowspan='3'><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"\n\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"\n\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n";

		Page page =new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testMultTables() {
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td><p> some colspan (note the double pipe) ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td><p> this cell spans vertically ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 8 some colspan (note the double pipe) ::UWCTOKENCOLSPANS:3::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 9 Col 1    ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 10 Col 2        ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                        </p></td>\n" + 
				"<td><p> Row 10 Col 2        ::UWCTOKENCOLSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"\n" + 
				"\n" + 
				"\n" +
				"<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     ::UWCTOKENROWSPANS:4::</p></td>\n" + 
				"<td><p> Row 1 Col 3        ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td><p> ::: </p></td>\n" + 
				"<td><p> ::: </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> ::: </p></td>\n" + 
				"<td><p> ::: </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td><p> ::: </p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td><p> Row 1 Col 2     </p></td>\n" + 
				"<td><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"<td colspan='2'><p> some colspan (note the double pipe) </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"<td><p> Row 3 Col 2     </p></td>\n" + 
				"<td><p> Row 3 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"<td rowspan='3'><p> this cell spans vertically </p></td>\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 5 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 5 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 6 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 6 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 7 Col 1    </p></td>\n" + 
				"<td><p> Row 7 Col 2     </p></td>\n" + 
				"<td><p> Row 7 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td colspan='3'><p> Row 8 some colspan (note the double pipe) </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='3'><p> Row 9 Col 1    </p></td>\n" + 
				"<td><p> Row 9 Col 2     </p></td>\n" + 
				"<td><p> Row 9 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td colspan='2'><p> Row 10 Col 2        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td colspan='2'><p> Row 10 Col 2        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" +
				"\n\n\n" +
				"<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Heading 1      </p></th>\n" + 
				"<th><p> Heading 2       </p></th>\n" + 
				"<th><p> Heading 3          </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 1 Col 1    </p></td>\n" + 
				"<td rowspan='4'><p> Row 1 Col 2     </p></td>\n" + 
				"<td rowspan='3'><p> Row 1 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 2 Col 1    </p></td>\n" + 
				"\n\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 3 Col 1    </p></td>\n" + 
				"\n\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> Row 4 Col 1    </p></td>\n" + 
				"\n" + 
				"<td><p> Row 4 Col 3        </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultTables_RowsProblem() {

		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> h1            </p></th>\n" + 
				"<th><p> h2 </p></th>\n" + 
				"<th><p> h3            </p></th>\n" + 
				"<th><p> h4                </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> foo           ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> bar               </p></td>\n" + 
				"<td><p> baz      ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> tralala          </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::                </p></td>\n" + 
				"<td><p> meh               </p></td>\n" + 
				"<td><p> :::                </p></td>\n" + 
				"<td><p> meep          </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> h1            </p></th>\n" + 
				"<th><p> h2 </p></th>\n" + 
				"<th><p> h3            </p></th>\n" + 
				"<th><p> h4                </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='2'><p> foo           </p></td>\n" + 
				"<td><p> bar               </p></td>\n" + 
				"<td rowspan='2'><p> baz      </p></td>\n" + 
				"<td><p> tralala          </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" +
				"\n" + 
				"<td><p> meh               </p></td>\n" +
				"\n" + 
				"<td><p> meep          </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testMultTables_RowsProblem2() {

		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> h1       </p></th>\n" + 
				"<th><p> h2                                 </p></th>\n" + 
				"<th><p> h3 </p></th>\n" + 
				"<th><p> h4 </p></th>\n" + 
				"<th><p> h5 </p></th>\n" + 
				"<th><p> h6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> thin        ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> man </p></td>\n" + 
				"<td><p> starring ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> myrna </p></td>\n" + 
				"<td><p> loy  ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> tralalala  ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> and  </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p> asta       </p></td>\n" + 
				"<td><p> :::                                      </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> foo         ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> bar     </p></td>\n" + 
				"<td><p> arg::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> this </p></td>\n" + 
				"<td><p> is ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> annoying ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> testing  </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p> 123      </p></td>\n" + 
				"<td><p> :::                                      </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> 1      ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> 2    </p></td>\n" + 
				"<td><p> 3   ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> 4  </p></td>\n" + 
				"<td><p> 5 ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"<td><p> 6  ::UWCTOKENROWSPANS:2::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> a </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p> b        </p></td>\n" + 
				"<td><p> :::                                      </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> h1       </p></th>\n" + 
				"<th><p> h2                                 </p></th>\n" + 
				"<th><p> h3 </p></th>\n" + 
				"<th><p> h4 </p></th>\n" + 
				"<th><p> h5 </p></th>\n" + 
				"<th><p> h6 </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='2'><p> thin        </p></td>\n" + 
				"<td><p> man </p></td>\n" + 
				"<td rowspan='2'><p> starring </p></td>\n" + 
				"<td><p> myrna </p></td>\n" + 
				"<td rowspan='2'><p> loy  </p></td>\n" + 
				"<td rowspan='2'><p> tralalala  </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" + 
				"<td><p> and  </p></td>\n" + 
				"\n" + 
				"<td><p> asta       </p></td>\n" + 
				"\n" + 
				"\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='2'><p> foo         </p></td>\n" + 
				"<td><p> bar     </p></td>\n" + 
				"<td rowspan='2'><p> arg</p></td>\n" + 
				"<td><p> this </p></td>\n" + 
				"<td rowspan='2'><p> is </p></td>\n" + 
				"<td rowspan='2'><p> annoying </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" +  
				"<td><p> testing  </p></td>\n" + 
				"\n" +  
				"<td><p> 123      </p></td>\n" + 
				"\n" +  
				"\n" +  
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='2'><p> 1      </p></td>\n" + 
				"<td><p> 2    </p></td>\n" + 
				"<td rowspan='2'><p> 3   </p></td>\n" + 
				"<td><p> 4  </p></td>\n" + 
				"<td rowspan='2'><p> 5 </p></td>\n" + 
				"<td rowspan='2'><p> 6  </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"\n" +   
				"<td><p> a </p></td>\n" + 
				"\n" +   
				"<td><p> b        </p></td>\n" + 
				"\n" +   
				"\n" +   
				"</tr>\n" + 
				"</tbody></table>\n"; 
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertColSpansWithHeader() {
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> Head 1 ::UWCTOKENCOLSPANS:6:: </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Row 1 </p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Header </p></th>\n" + 
				"<td><p> Colspan Here ::UWCTOKENCOLSPANS:5::</p></td>\n" + 
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th colspan='6'><p> Head 1  </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Row 1 </p></th>\n" + 
				"<td> Row 2 </td>\n" + 
				"<td> Row 3 </td>\n" +
				"<td> Row 4 </td>\n" +
				"<td> Row 5 </td>\n" +
				"<td> Row 6 </td>\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" +
				"<td><p> Row 2 </p></td>\n" + 
				"<td><p> Row 3 </p></td>\n" +
				"<td><p> Row 4 </p></td>\n" +
				"<td><p> Row 5 </p></td>\n" +
				"<td><p> Row 6 </p></td>\n" +
				"</tr>\n" + 
				"<tr>\n" +
				"<th><p> :::          </p></th>\n" +
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Item 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> :::          </p></th>\n" +
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td><p>&nbsp;</p></td>\n" + 
				"<td> Last 6 </td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> Header </p></th>\n" + 
				"<td colspan='5'><p> Colspan Here </p></td>\n" +
				"</tr>\n" + 
				"</tbody></table>\n" + 
				"";
		actual = tester.convertColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	
	public void testConvertMoreRowSpanTroubles() {
		String input, expected, actual;
		input = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th><p> HEADER         ::UWCTOKENCOLSPANS:6::</p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> h1       </p></th>\n" + 
				"<th><p> h2                                                </p></th>\n" + 
				"<th><p> h3                             </p></th>\n" + 
				"<th><p> h4                                        </p></th>\n" + 
				"<th><p> h5                        </p></th>\n" + 
				"<th><p> h6        </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> r1c1        ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r1c2     </p></td>\n" + 
				"<td><p> r1c3     ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r1c4  </p></td>\n" + 
				"<td><p> r1c5  ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r1c6  ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> r2c2          </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p> r2c4        </p></td>\n" + 
				"<td><p> :::                               </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> r3c2          </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p>r3c4</p></td>\n" + 
				"<td><p> :::                               </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> r4c1         ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r4c2     </p></td>\n" + 
				"<td><p> r4c3     ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r4c4  </p></td>\n" + 
				"<td><p> r4c5  ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"<td><p> r4c6  ::UWCTOKENROWSPANS:3::</p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> r2c2          </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p> r2c4        </p></td>\n" + 
				"<td><p> :::                               </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td><p> :::           </p></td>\n" + 
				"<td><p> r3c2          </p></td>\n" + 
				"<td><p> :::                                       </p></td>\n" + 
				"<td><p>r3c4</p></td>\n" + 
				"<td><p> :::                               </p></td>\n" + 
				"<td><p> :::             </p></td>\n" + 
				"</tr>\n" + 
				"</table>\n" + 
				"";
		expected = "<table><tbody>\n" + 
				"<tr>\n" + 
				"<th colspan='6'><p> HEADER         </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<th><p> h1       </p></th>\n" + 
				"<th><p> h2                                                </p></th>\n" + 
				"<th><p> h3                             </p></th>\n" + 
				"<th><p> h4                                        </p></th>\n" + 
				"<th><p> h5                        </p></th>\n" + 
				"<th><p> h6        </p></th>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='3'><p> r1c1        </p></td>\n" + 
				"<td><p> r1c2     </p></td>\n" + 
				"<td rowspan='3'><p> r1c3     </p></td>\n" + 
				"<td><p> r1c4  </p></td>\n" + 
				"<td rowspan='3'><p> r1c5  </p></td>\n" + 
				"<td rowspan='3'><p> r1c6  </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" +
				"\n" + 
				"<td><p> r2c2          </p></td>\n" +
				"\n" +
				"<td><p> r2c4        </p></td>\n" +
				"\n" +
				"\n" +
				"</tr>\n" + 
				"<tr>\n" +
				"\n" +
				"<td><p> r3c2          </p></td>\n" +
				"\n" +
				"<td><p>r3c4</p></td>\n" +
				"\n" +
				"\n" +
				"</tr>\n" + 
				"<tr>\n" + 
				"<td rowspan='3'><p> r4c1         </p></td>\n" + 
				"<td><p> r4c2     </p></td>\n" + 
				"<td rowspan='3'><p> r4c3     </p></td>\n" + 
				"<td><p> r4c4  </p></td>\n" + 
				"<td rowspan='3'><p> r4c5  </p></td>\n" + 
				"<td rowspan='3'><p> r4c6  </p></td>\n" + 
				"</tr>\n" + 
				"<tr>\n" +
				"\n" + 
				"<td><p> r2c2          </p></td>\n" +
				"\n" +
				"<td><p> r2c4        </p></td>\n" +
				"\n" +
				"\n" +
				"</tr>\n" + 
				"<tr>\n" +
				"\n" +
				"<td><p> r3c2          </p></td>\n" +
				"\n" +
				"<td><p>r3c4</p></td>\n" +
				"\n" +
				"\n" +
				"</tr>\n" + 
				"</table>\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
