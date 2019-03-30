package files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import counters.Counter;
import counters.CounterManager;
import filters.Filter;
import filters.FilterManager;
import image_viewer.Viewer;
import image_viewer.Window;

/**
 * This object can be combined with a Window containing viewers to apply the
 * contents of this file to the viewers already in the window. It can also be
 * used to store the contents of a current window in order to store it in the
 * IMF file format. The IMF file format is a binary XML file.
 * 
 * @author gregfoss
 *
 */
public class IMFFile {

	private Document imf_file = null;

	/**
	 * Constructor for establishing a virtual IMF file for intuitive viewer storage
	 * or saving.
	 * 
	 * @param window     - the window to record into an IMF file.
	 * @param lock_names - the parameter to determine if the names of each open
	 *                   viewer will be recorded in the file. This should be false
	 *                   for most normal files and true for nd2 files.
	 * @throws ParserConfigurationException
	 */
	public IMFFile(Window window, boolean lock_names) throws ParserConfigurationException {
		Viewer[] viewers = window.get_viewers();

		// Managers that will used to get string identifiers.
		FilterManager filter_manager = new FilterManager();
		CounterManager counter_manager = new CounterManager();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		this.imf_file = builder.newDocument();

		// Creating the root or the document:
		Element root = this.imf_file.createElement("imf_file");
		this.imf_file.appendChild(root);

		// Iterating over all the viewers and adding them to the file:
		for (Viewer viewer : viewers) {
			Element image_element = this.imf_file.createElement("image");
			root.appendChild(image_element);
			if (lock_names) {
				Attr name_attr = this.imf_file.createAttribute("name");
				name_attr.setValue(viewer.KEY);
				image_element.setAttributeNode(name_attr);
			}

			// Building the filter list.
			Element filter_list_element = this.imf_file.createElement("filter_list");
			image_element.appendChild(filter_list_element);
			for (Filter filter : viewer.get_filters()) {
				// Creating the filter element:
				Element filter_element = this.imf_file.createElement("filter");
				filter_list_element.appendChild(filter_element);

				// Setting the filters' types:
				Attr type_attr = this.imf_file.createAttribute("type");
				type_attr.setValue(FilterManager.get_filter_key(filter));
				filter_element.setAttributeNode(type_attr);

				// Adding the filter's parameters
				filter_element.appendChild(this.imf_file.createTextNode(filter.get_params()));
			}

			// Creating the counter:
			Element counter_element = this.imf_file.createElement("counter");
			image_element.appendChild(counter_element);
			Attr type_attr = this.imf_file.createAttribute("type");
			type_attr.setValue(counter_manager.get_key(viewer.get_counter()));
			counter_element.setAttributeNode(type_attr);

			// Adding the counter settings:
			String[] counter_settings = viewer.get_counter().encode();
			if (counter_settings != null)
				counter_element.appendChild(this.imf_file.createTextNode(String.join(",", counter_settings)));
		}
	}

	/**
	 * Constructor for building an IMF file object from an existing IMF file.
	 * 
	 * @param path_to_file
	 * @throws IOException
	 */
	public IMFFile(File to_read) throws IOException {
		System.out.print("Loading " + to_read.getAbsolutePath() + ".imf {");
		Document imf_file = IMFFile.parse(to_read);
		System.out.println("} Finished Parsing.");
		if (imf_file == null)
			throw new IOException();
		this.imf_file = imf_file;
	}

	public static Document parse(File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			return document;
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		System.err.println("[ERROR]: Problem occured while parsing " + file.getAbsolutePath());
		return null;
	}
	
	public void apply_to_window(Window window) {
		this.apply_to_viewers(window.get_viewers());
		window.repaint();
	}

	/**
	 * Applies this IMF file to the contents of the window
	 * 
	 * @param viewers - the array of viewers to apply to.
	 */
	public void apply_to_viewers(Viewer[] viewers) {
		System.out.println("Applying imf to viewers...");
		Element imf_root = (Element) this.imf_file.getFirstChild();
		NodeList images = imf_root.getChildNodes();

		int viewer_number = viewers.length;
		if (viewer_number != (int) (images.getLength() / 2))
			System.err.printf(
					"[WARNING]: Number of images expected in the .imf file is %d and the number of open images is %d.\n",
					(int) (images.getLength() / 2), viewer_number);

		int image_index = 0;
		boolean strong_names = false;

		for (int i = 0; i < images.getLength(); i++) {
			if (images.item(i).getNodeType() == Node.ELEMENT_NODE) {
				// Now we have our image root node:
				Element image = (Element) images.item(i);

				// Checking if the images have strongly specified names:
				strong_names = image.hasAttribute("name");

				// Getting the filter list and counter elements:
				Element filter_list = null, counter = null;
				for (int n = 0; n < image.getChildNodes().getLength(); n++)
					if (image.getChildNodes().item(n).getNodeType() == Node.ELEMENT_NODE)
						if (filter_list == null)
							filter_list = (Element) image.getChildNodes().item(n);
						else
							counter = (Element) image.getChildNodes().item(n);
				if (filter_list.getNodeName().equals("counter")) {
					Element holder = filter_list;
					filter_list = counter;
					counter = holder;
				}

				CounterManager counter_manager = new CounterManager();

				// If there are strong names then we set the image index to the image with this
				// name:
				String image_name = "";
				if (strong_names) {
					image_index = viewers.length;
					image_name = image.getAttribute("name");
					for (int k = 0; k < viewers.length; k++)
						if (viewers[k].KEY.equals(image_name))
							image_index = k;
				}

				// Applying the counter and filters to the viewer if the index i is less than
				// the number of viewers:
				System.out.println("Entering edit mode.");
				viewers[image_index].set_edit_mode(true);
				if (image_index < viewers.length) {
					viewers[image_index].clear_filters();

					// Applying the filter list:
					List<Filter> filters = new ArrayList<Filter>();
					for (int n = 0; n < filter_list.getChildNodes().getLength(); n++)
						if (filter_list.getChildNodes().item(n).getNodeType() == Node.ELEMENT_NODE) {
							Element filter_element = (Element) filter_list.getChildNodes().item(n);

							// Now that we have the filter itself, we have to get its name first:
							String filter_name = filter_element.getAttribute("type");

							// Now we have to get its contents:
							String contents = filter_element.getTextContent();

							// Storing this filter to add later:
							filters.add(FilterManager.get_filter(filter_name));
							filters.get(filters.size() - 1).set_params(contents);
						}
					// Adding all the filters to the viewer:
					System.out.println("Adding filters.");
					viewers[image_index].add_filters(filters);

					// Applying the counter:
					System.out.println("Applying the counter.");
					Counter counter_object = counter_manager.get_new_counter(counter.getAttribute("type"));
					counter_object.decode(counter.getTextContent().split(","));
					viewers[image_index].set_counter(counter_object);
					System.out.println("Exiting edit mode.");
					viewers[image_index].set_edit_mode(false);
					image_index++;
				} else if (strong_names) {
					System.err.printf("Image %s is not currently open.\n", image_name);
				}
			}
		}
		
		// Viewers must now have their filters updated and their counts updated.
		for (Viewer viewer : viewers) {
			viewer.point_out_blobs();
			viewer.repaint();
		}
		System.out.println("Finished applying to viewers.");
	}

	/**
	 * Saves the IMF file stored in this object to a .imf file at path_to_save_to.
	 * 
	 * @param path_to_save_to
	 * @param overwrite       whether or not to overwrite the file if it exists:
	 * @return 0 if saved successfully, 1 if file already exists, 2 if some other
	 *         exception occurs
	 */
	public int save_file(String path_to_save_to, boolean overwrite) {
		File saved_file = new File(path_to_save_to);
		if (!overwrite && saved_file.exists())
			return 1;

		TransformerFactory transform_factory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transform_factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource dom_source = new DOMSource(this.imf_file);
			StreamResult streamResult = new StreamResult(saved_file);
			transformer.transform(dom_source, streamResult);
			return 0;
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return 2;
	}

}
