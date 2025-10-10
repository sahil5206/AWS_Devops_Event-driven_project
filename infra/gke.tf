resource "google_container_cluster" "gke_cluster" {
    name = "sahil-gke-cluster"
    location = var.zone
    network = google_compute_network.vpc_network.name
    subnetwork = google_compute_subnetwork.subnet.name

    remove_default_node_pool = true
    initial_node_count = 1
    ip_allocation_policy {}
}

resource "google_contaner_node_pool" "primary_nodes" {
    name = "primary-node-pool"
    cluster = google_container_cluster.gke_cluster.name
    location = var.zone
    node_count = var.gke_node_count

    node_config {
        machine_type = var.gke_machine_type
        oauth_scopes = ["https://www.googleapis.com/auth/cloud-platform"]
    }
}