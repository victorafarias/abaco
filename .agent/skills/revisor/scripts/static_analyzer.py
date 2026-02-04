#!/usr/bin/env python3
"""
Static Code Analyzer for AI-Generated Code
Performs automated checks for common issues, security vulnerabilities, and code quality.
"""

import os
import json
import argparse
import subprocess
import sys
from pathlib import Path
from typing import Dict, List, Any

class StaticAnalyzer:
    """Analyzes code for common issues and generates a report."""
    
    def __init__(self, code_dir: str, language: str):
        self.code_dir = Path(code_dir)
        self.language = language.lower()
        self.results = {
            "summary": {},
            "issues": [],
            "metrics": {},
            "security": []
        }
    
    def analyze(self) -> Dict[str, Any]:
        """Run all analysis checks."""
        print(f"Analyzing {self.language} code in {self.code_dir}...")
        
        if self.language == "python":
            self._analyze_python()
        elif self.language in ["javascript", "typescript", "js", "ts"]:
            self._analyze_javascript()
        elif self.language == "java":
            self._analyze_java()
        else:
            self._analyze_generic()
        
        self._calculate_summary()
        return self.results
    
    def _analyze_python(self):
        """Python-specific analysis."""
        # Run pylint if available
        try:
            result = subprocess.run(
                ["pylint", str(self.code_dir), "--output-format=json"],
                capture_output=True,
                text=True,
                timeout=30
            )
            if result.stdout:
                pylint_issues = json.loads(result.stdout)
                for issue in pylint_issues:
                    self.results["issues"].append({
                        "tool": "pylint",
                        "file": issue.get("path", ""),
                        "line": issue.get("line", 0),
                        "severity": issue.get("type", ""),
                        "message": issue.get("message", ""),
                        "symbol": issue.get("symbol", "")
                    })
        except (subprocess.TimeoutExpired, FileNotFoundError, json.JSONDecodeError):
            print("Warning: pylint not available or failed")
        
        # Run bandit for security if available
        try:
            result = subprocess.run(
                ["bandit", "-r", str(self.code_dir), "-f", "json"],
                capture_output=True,
                text=True,
                timeout=30
            )
            if result.stdout:
                bandit_results = json.loads(result.stdout)
                for issue in bandit_results.get("results", []):
                    self.results["security"].append({
                        "tool": "bandit",
                        "file": issue.get("filename", ""),
                        "line": issue.get("line_number", 0),
                        "severity": issue.get("issue_severity", ""),
                        "confidence": issue.get("issue_confidence", ""),
                        "message": issue.get("issue_text", "")
                    })
        except (subprocess.TimeoutExpired, FileNotFoundError, json.JSONDecodeError):
            print("Warning: bandit not available or failed")
        
        # Calculate basic metrics
        self._calculate_metrics_python()
    
    def _analyze_javascript(self):
        """JavaScript/TypeScript analysis."""
        # Run eslint if available
        try:
            result = subprocess.run(
                ["eslint", str(self.code_dir), "--format=json"],
                capture_output=True,
                text=True,
                timeout=30
            )
            if result.stdout:
                eslint_results = json.loads(result.stdout)
                for file_result in eslint_results:
                    for message in file_result.get("messages", []):
                        self.results["issues"].append({
                            "tool": "eslint",
                            "file": file_result.get("filePath", ""),
                            "line": message.get("line", 0),
                            "severity": "error" if message.get("severity") == 2 else "warning",
                            "message": message.get("message", ""),
                            "rule": message.get("ruleId", "")
                        })
        except (subprocess.TimeoutExpired, FileNotFoundError, json.JSONDecodeError):
            print("Warning: eslint not available or failed")
        
        self._calculate_metrics_javascript()
    
    def _analyze_java(self):
        """Java-specific analysis."""
        print("Java analysis: Using generic checks (install checkstyle for detailed analysis)")
        self._analyze_generic()
    
    def _analyze_generic(self):
        """Generic code analysis for any language."""
        # Basic file analysis
        for filepath in self.code_dir.rglob("*"):
            if filepath.is_file() and not filepath.name.startswith('.'):
                self._check_file_basics(filepath)
    
    def _check_file_basics(self, filepath: Path):
        """Check basic file properties."""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                lines = f.readlines()
                
            # Check for very long files
            if len(lines) > 500:
                self.results["issues"].append({
                    "tool": "basic",
                    "file": str(filepath),
                    "line": 0,
                    "severity": "warning",
                    "message": f"File is very long ({len(lines)} lines). Consider splitting."
                })
            
            # Check for very long lines
            for i, line in enumerate(lines, 1):
                if len(line) > 120:
                    self.results["issues"].append({
                        "tool": "basic",
                        "file": str(filepath),
                        "line": i,
                        "severity": "info",
                        "message": f"Line exceeds 120 characters ({len(line)} chars)"
                    })
                
                # Check for common security patterns
                line_lower = line.lower()
                if any(pattern in line_lower for pattern in ['password', 'api_key', 'secret']) and '=' in line:
                    if not any(safe in line_lower for safe in ['env', 'config', 'get(']):
                        self.results["security"].append({
                            "tool": "basic",
                            "file": str(filepath),
                            "line": i,
                            "severity": "high",
                            "message": "Possible hardcoded credential"
                        })
        
        except Exception as e:
            print(f"Warning: Could not analyze {filepath}: {e}")
    
    def _calculate_metrics_python(self):
        """Calculate Python-specific metrics."""
        total_lines = 0
        total_files = 0
        
        for filepath in self.code_dir.rglob("*.py"):
            try:
                with open(filepath, 'r') as f:
                    lines = [l for l in f.readlines() if l.strip() and not l.strip().startswith('#')]
                    total_lines += len(lines)
                    total_files += 1
            except:
                pass
        
        self.results["metrics"] = {
            "total_files": total_files,
            "total_lines": total_lines,
            "avg_lines_per_file": total_lines / total_files if total_files > 0 else 0
        }
    
    def _calculate_metrics_javascript(self):
        """Calculate JavaScript-specific metrics."""
        total_lines = 0
        total_files = 0
        
        for filepath in self.code_dir.rglob("*.js"):
            try:
                with open(filepath, 'r') as f:
                    lines = [l for l in f.readlines() if l.strip() and not l.strip().startswith('//')]
                    total_lines += len(lines)
                    total_files += 1
            except:
                pass
        
        self.results["metrics"] = {
            "total_files": total_files,
            "total_lines": total_lines,
            "avg_lines_per_file": total_lines / total_files if total_files > 0 else 0
        }
    
    def _calculate_summary(self):
        """Calculate summary statistics."""
        errors = sum(1 for i in self.results["issues"] if i.get("severity") in ["error", "high"])
        warnings = sum(1 for i in self.results["issues"] if i.get("severity") in ["warning", "medium"])
        info = sum(1 for i in self.results["issues"] if i.get("severity") in ["info", "low"])
        
        security_high = sum(1 for s in self.results["security"] if s.get("severity") == "high")
        security_medium = sum(1 for s in self.results["security"] if s.get("severity") == "medium")
        
        self.results["summary"] = {
            "total_issues": len(self.results["issues"]),
            "errors": errors,
            "warnings": warnings,
            "info": info,
            "security_issues": len(self.results["security"]),
            "security_high": security_high,
            "security_medium": security_medium
        }


def main():
    parser = argparse.ArgumentParser(description="Static code analyzer for AI-generated code")
    parser.add_argument("code_dir", help="Directory containing code to analyze")
    parser.add_argument("--language", required=True, help="Programming language (python, javascript, java, etc.)")
    parser.add_argument("--output", default="analysis_report.json", help="Output file for results")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.code_dir):
        print(f"Error: Directory {args.code_dir} does not exist")
        sys.exit(1)
    
    analyzer = StaticAnalyzer(args.code_dir, args.language)
    results = analyzer.analyze()
    
    # Save results
    with open(args.output, 'w') as f:
        json.dump(results, f, indent=2)
    
    # Print summary
    print("\n" + "="*60)
    print("ANALYSIS SUMMARY")
    print("="*60)
    print(f"Total Issues: {results['summary']['total_issues']}")
    print(f"  Errors: {results['summary']['errors']}")
    print(f"  Warnings: {results['summary']['warnings']}")
    print(f"  Info: {results['summary']['info']}")
    print(f"\nSecurity Issues: {results['summary']['security_issues']}")
    print(f"  High Severity: {results['summary']['security_high']}")
    print(f"  Medium Severity: {results['summary']['security_medium']}")
    print(f"\nMetrics:")
    for key, value in results['metrics'].items():
        print(f"  {key}: {value}")
    print(f"\nDetailed results saved to: {args.output}")
    print("="*60)

if __name__ == "__main__":
    main()
