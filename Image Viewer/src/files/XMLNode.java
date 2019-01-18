package files;

import java.util.LinkedList;
import java.util.List;

public class XMLNode {

	public static final int XML_FILE = 0;
	public static final int IMAGE = 1;
	public static final int FILTER_CHAIN = 2;
	public static final int COUNT_METHOD = 3;
	public static final int FILTER = 4;
	public static final int LEAF = 5;

	private List<XMLNode> children;

	// Variables that store the makeup of this XML node:
	// The parameters stores values like "type=Multiply"
	private String[] params;
	private int type;
	private boolean is_single_line = true, draw_tags = true;

	public XMLNode(int type, String[] params, boolean single_line) {
		this(type, params, single_line, true);
	}

	/**
	 * Constructs an XML tree:
	 * 
	 * @param type
	 * @param params
	 * @param single_line
	 */
	public XMLNode(int type, String[] params, boolean single_line, boolean draw_tags) {
		this.is_single_line = single_line;
		this.type = type;
		this.params = params;
		this.draw_tags = draw_tags;
		if (this.params == null)
			this.params = new String[] {};
		this.children = new LinkedList<XMLNode>();
	}

	public void add_child(XMLNode child) {
		this.children.add(child);
	}

	public String get_xml_string(int tabs) {
		String tag = "";

		// Stitching the child XML node strings together:
		String body = "";
		for (XMLNode child : this.children)
			body += child.get_xml_string(tabs + 1);

		// If this node makes up a leaf body:
		if (this.type == LEAF)
			return String.join(",", this.params);

		// The default parameter string for XML tags without parameters
		String params_str = "";

		// The following code is for regular XML tags that have arguments:
		if (this.draw_tags && this.params.length > 0) {
			params_str = " ";
			for (int i = 0; i < this.params.length; i++) {
				params_str += String.format("%s", this.params[i]);
				if (i != this.params.length - 1)
					params_str += " ";
			}
		}

		switch (this.type) {
		case IMAGE:
			tag = "image";
			break;
		case FILTER_CHAIN:
			tag = "filter_chain";
			break;
		case COUNT_METHOD:
			tag = "count_method";
			break;
		case FILTER:
			tag = "filter";
			break;
		default:
			return body;
		}

		StringBuilder tabs_builder = new StringBuilder("");
		for (int i = 0; i < tabs; i++)
			tabs_builder.append("\t");
		String header = String.format("%s<%s%s>", tabs_builder.toString(), tag, params_str);
		String tail = String.format("%s</%s>\n", this.is_single_line ? "" : tabs_builder.toString(), tag);

		String separater = (this.is_single_line ? "" : "\n");
		StringBuilder builder = new StringBuilder(header);
		builder.append(separater).append(body).append(tail);

		return builder.toString();
	}

}
