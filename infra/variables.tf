variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "availability_zone_a" {
  description = "Primary availability zone for the first public subnet"
  type        = string
  default     = "us-east-1a"
}

variable "availability_zone_b" {
  description = "Secondary availability zone for the second public subnet"
  type        = string
  default     = "us-east-1b"
}

variable "aws_profile" {
  description = "Optional named AWS CLI profile to use for authentication"
  type        = string
  default     = null
}

variable "vpc_cidr" {
  description = "Primary CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_a_cidr" {
  description = "CIDR block for the first public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "public_subnet_b_cidr" {
  description = "CIDR block for the second public subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "eks_cluster_name" {
  description = "Name for the EKS cluster"
  type        = string
  default     = "event-driven-eks"
}

variable "eks_version" {
  description = "Kubernetes version for the EKS control plane"
  type        = string
  default     = "1.29"
}

variable "eks_node_instance_type" {
  description = "Instance type for worker nodes"
  type        = string
  default     = "t3.medium"
}

variable "eks_desired_capacity" {
  description = "Desired number of nodes in the node group"
  type        = number
  default     = 2
}

variable "eks_min_size" {
  description = "Minimum nodes in the node group"
  type        = number
  default     = 1
}

variable "eks_max_size" {
  description = "Maximum nodes in the node group"
  type        = number
  default     = 3
}

