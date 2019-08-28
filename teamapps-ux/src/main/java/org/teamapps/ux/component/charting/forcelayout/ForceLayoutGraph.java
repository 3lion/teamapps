package org.teamapps.ux.component.charting.forcelayout;

import org.jetbrains.annotations.NotNull;
import org.teamapps.data.extract.BeanPropertyExtractor;
import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.dto.UiClientRecord;
import org.teamapps.dto.UiComponent;
import org.teamapps.dto.UiEvent;
import org.teamapps.dto.UiNetworkGraph;
import org.teamapps.dto.UiNetworkLink;
import org.teamapps.dto.UiNetworkNode;
import org.teamapps.event.Event;
import org.teamapps.util.UiUtil;
import org.teamapps.ux.component.AbstractComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForceLayoutGraph<RECORD> extends AbstractComponent {

	public final Event<ForceLayoutNode> onNodeClicked = new Event<>();
	public final Event<NodeExpandedOrCollapsedEvent<RECORD>> onNodeExpandedOrCollapsed = new Event<>();

	private final List<ForceLayoutNode<RECORD>> nodes;
	private final List<ForceLayoutLink> links;

	private int animationDuration = 1000;
	// private float gravity = 0.1f;
	// private float theta = 0.3f;
	// private float alpha = 0.1f;
	// private int charge = -300;
	// private int distance = 30;
	// private Color highlightColor;
	
	private PropertyExtractor<RECORD> propertyExtractor = new BeanPropertyExtractor<>();

	public ForceLayoutGraph() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	public ForceLayoutGraph(List<ForceLayoutNode<RECORD>> nodes, List<ForceLayoutLink> links) {
		this.nodes = new ArrayList<>(nodes);
		this.links = new ArrayList<>(links);
	}

	@Override
	public UiComponent createUiComponent() {
		List<UiNetworkNode> nodes = createUiNodes(this.nodes);
		List<UiNetworkLink> links = createUiLinks(this.links);
		UiNetworkGraph ui = new UiNetworkGraph(nodes, links, Collections.emptyList());
		ui.setAnimationDuration(animationDuration);
		mapAbstractUiComponentProperties(ui);
		return ui;
	}

	@NotNull
	private List<UiNetworkNode> createUiNodes(List<ForceLayoutNode<RECORD>> nodes) {
		return nodes.stream()
				.map(n -> createUiNode(n))
				.collect(Collectors.toList());
	}

	@NotNull
	private List<UiNetworkLink> createUiLinks(List<ForceLayoutLink> links) {
		return links.stream()
				.map(l -> l.toUiNetworkLink())
				.collect(Collectors.toList());
	}

	@NotNull
	private UiNetworkNode createUiNode(ForceLayoutNode<RECORD> node) {
		UiNetworkNode uiNode = new UiNetworkNode(node.getId(), node.getWidth(), node.getHeight());
		uiNode.setBackgroundColor(node.getBackgroundColor() != null ? UiUtil.createUiColor(node.getBackgroundColor()) : null);
		uiNode.setBorderColor(node.getBorderColor() != null ? UiUtil.createUiColor(node.getBorderColor()) : null);
		uiNode.setBorderWidth(node.getBorderWidth());
		uiNode.setBorderRadius(node.getBorderRadius());
		uiNode.setTemplate(node.getTemplate() != null ? node.getTemplate().createUiTemplate() : null);
		uiNode.setRecord(node.getRecord() != null ? createUiRecord(node) : null);
		uiNode.setExpandState(node.getExpandedState().toExpandState());
		uiNode.setIcon(node.getIcon() != null ? node.getIcon().createUiTreeGraphNodeIcon() : null);
		uiNode.setImage(node.getImage() != null ? node.getImage().createUiTreeGraphNodeImage() : null);
		return uiNode;
	}

	private UiClientRecord createUiRecord(ForceLayoutNode<RECORD> node) {
		UiClientRecord uiClientRecord = new UiClientRecord();
		uiClientRecord.setValues(propertyExtractor.getValues(node.getRecord(), node.getTemplate().getDataKeys()));
		return uiClientRecord;
	}

	@Override
	public void handleUiEvent(UiEvent event) {
		switch (event.getUiEventType()) {
			case UI_NETWORK_GRAPH_NODE_CLICKED: {
				UiNetworkGraph.NodeClickedEvent clickEvent = (UiNetworkGraph.NodeClickedEvent) event;
				nodes.stream()
						.filter(n -> n.getId().equals(clickEvent.getNodeId()))
						.findFirst()
						.ifPresent(onNodeClicked::fire);
				break;
			}
			case UI_NETWORK_GRAPH_NODE_EXPANDED_OR_COLLAPSED:
				UiNetworkGraph.NodeExpandedOrCollapsedEvent clickEvent = (UiNetworkGraph.NodeExpandedOrCollapsedEvent) event;
				nodes.stream()
						.filter(n -> n.getId().equals(clickEvent.getNodeId()))
						.findFirst()
						.ifPresent(n -> onNodeExpandedOrCollapsed.fire(new NodeExpandedOrCollapsedEvent<RECORD>(n, clickEvent.getExpanded())));
				break;
		}
	}

	public int getAnimationDuration() {
		return animationDuration;
	}

	public void setAnimationDuration(int animationDuration) {
		this.animationDuration = animationDuration;
	}

	public void addNodesAndLinks(List<ForceLayoutNode<RECORD>> nodes, List<ForceLayoutLink> links) {
		this.nodes.addAll(nodes);
		this.links.addAll(links);
		queueCommandIfRendered(() -> new UiNetworkGraph.AddNodesAndLinksCommand(getId(), createUiNodes(nodes), createUiLinks(links)));
	}

	public void removeNodesAndLinks(List<ForceLayoutNode<RECORD>> nodes) {
		List<ForceLayoutLink> linksToRemove = links.stream()
				.filter(l -> nodes.contains(l.getSource()) || nodes.contains(l.getTarget()))
				.collect(Collectors.toList());
		System.out.println(linksToRemove);
		removeNodesAndLinks(nodes, linksToRemove);
	}

	public void removeNodesAndLinks(List<ForceLayoutNode<RECORD>> nodes, List<ForceLayoutLink> links) {
		this.nodes.removeAll(nodes);
		this.links.removeAll(links);
		List<String> nodeIds = nodes.stream().map(n -> n.getId()).collect(Collectors.toList());
		Map<String, List<String>> linksBySourceNodeId = links.stream()
				.collect(Collectors.groupingBy(l -> l.getSource().getId(), Collectors.mapping(l -> l.getTarget().getId(), Collectors.toList())));
		queueCommandIfRendered(() -> new UiNetworkGraph.RemoveNodesAndLinksCommand(getId(), nodeIds, linksBySourceNodeId));
	}

	public List<ForceLayoutNode<RECORD>> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public List<ForceLayoutLink> getLinks() {
		return Collections.unmodifiableList(links);
	}

	public PropertyExtractor<RECORD> getPropertyExtractor() {
		return propertyExtractor;
	}
	public void setPropertyExtractor(PropertyExtractor<RECORD> propertyExtractor) {
		this.propertyExtractor = propertyExtractor;
	}
}