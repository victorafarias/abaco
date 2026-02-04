#!/usr/bin/env python3
"""
Test Runner for Code Review
Executes tests and generates a summary report.
"""

import argparse
import subprocess
import json
import sys
from pathlib import Path

class TestRunner:
    """Run tests and collect results."""
    
    def __init__(self, code_dir, framework):
        self.code_dir = Path(code_dir)
        self.framework = framework.lower()
        self.results = {
            "framework": framework,
            "passed": 0,
            "failed": 0,
            "skipped": 0,
            "errors": 0,
            "failures": [],
            "duration": 0
        }
    
    def run(self):
        """Execute tests based on framework."""
        if self.framework == "pytest":
            return self._run_pytest()
        elif self.framework == "unittest":
            return self._run_unittest()
        elif self.framework == "jest":
            return self._run_jest()
        elif self.framework == "junit":
            return self._run_junit()
        else:
            print(f"Unsupported framework: {self.framework}")
            return False
    
    def _run_pytest(self):
        """Run pytest tests."""
        try:
            result = subprocess.run(
                ["pytest", str(self.code_dir), "-v", "--json-report", "--json-report-file=test_results.json"],
                capture_output=True,
                text=True,
                timeout=120
            )
            
            # Try to load JSON report
            try:
                with open("test_results.json", 'r') as f:
                    data = json.load(f)
                    self.results["passed"] = data.get("summary", {}).get("passed", 0)
                    self.results["failed"] = data.get("summary", {}).get("failed", 0)
                    self.results["skipped"] = data.get("summary", {}).get("skipped", 0)
                    self.results["duration"] = data.get("duration", 0)
                    
                    for test in data.get("tests", []):
                        if test.get("outcome") == "failed":
                            self.results["failures"].append({
                                "name": test.get("nodeid", ""),
                                "message": test.get("call", {}).get("longrepr", "")
                            })
            except:
                # Fallback to parsing output
                self._parse_pytest_output(result.stdout)
            
            return result.returncode == 0
            
        except subprocess.TimeoutExpired:
            print("Test execution timed out")
            return False
        except FileNotFoundError:
            print("pytest not found. Install with: pip install pytest pytest-json-report")
            return False
    
    def _parse_pytest_output(self, output):
        """Parse pytest text output."""
        for line in output.split('\n'):
            if 'passed' in line.lower():
                # Try to extract numbers
                parts = line.split()
                for i, part in enumerate(parts):
                    if part.isdigit():
                        if 'passed' in line[:line.index(part)].lower():
                            self.results["passed"] = int(part)
                        elif 'failed' in line[:line.index(part)].lower():
                            self.results["failed"] = int(part)
    
    def _run_unittest(self):
        """Run unittest tests."""
        try:
            result = subprocess.run(
                ["python", "-m", "unittest", "discover", str(self.code_dir), "-v"],
                capture_output=True,
                text=True,
                timeout=120
            )
            
            # Parse output
            output = result.stderr + result.stdout
            for line in output.split('\n'):
                if line.startswith('Ran'):
                    parts = line.split()
                    if len(parts) >= 2:
                        total = int(parts[1])
                if 'OK' in line:
                    self.results["passed"] = total if 'total' in locals() else 0
                elif 'FAILED' in line:
                    # Extract failure count
                    parts = line.split()
                    for part in parts:
                        if part.startswith('failures='):
                            self.results["failed"] = int(part.split('=')[1].rstrip(','))
            
            return result.returncode == 0
            
        except subprocess.TimeoutExpired:
            print("Test execution timed out")
            return False
        except FileNotFoundError:
            print("Python unittest not found")
            return False
    
    def _run_jest(self):
        """Run jest tests."""
        try:
            result = subprocess.run(
                ["jest", "--json", "--outputFile=test_results.json"],
                cwd=str(self.code_dir),
                capture_output=True,
                text=True,
                timeout=120
            )
            
            # Load JSON report
            try:
                with open(self.code_dir / "test_results.json", 'r') as f:
                    data = json.load(f)
                    self.results["passed"] = data.get("numPassedTests", 0)
                    self.results["failed"] = data.get("numFailedTests", 0)
                    self.results["duration"] = data.get("testResults", [{}])[0].get("perfStats", {}).get("runtime", 0) / 1000
                    
                    for test_result in data.get("testResults", []):
                        for test in test_result.get("assertionResults", []):
                            if test.get("status") == "failed":
                                self.results["failures"].append({
                                    "name": test.get("fullName", ""),
                                    "message": "\n".join(test.get("failureMessages", []))
                                })
            except:
                pass
            
            return result.returncode == 0
            
        except subprocess.TimeoutExpired:
            print("Test execution timed out")
            return False
        except FileNotFoundError:
            print("jest not found. Install with: npm install --save-dev jest")
            return False
    
    def _run_junit(self):
        """Run JUnit tests."""
        print("JUnit testing requires specific project setup. Please run tests manually.")
        return False
    
    def print_summary(self):
        """Print test summary."""
        print("\n" + "="*60)
        print("TEST RESULTS SUMMARY")
        print("="*60)
        print(f"Framework: {self.framework}")
        print(f"Passed: {self.results['passed']}")
        print(f"Failed: {self.results['failed']}")
        print(f"Skipped: {self.results['skipped']}")
        
        if self.results['duration'] > 0:
            print(f"Duration: {self.results['duration']:.2f}s")
        
        if self.results['failures']:
            print("\nFailed Tests:")
            for i, failure in enumerate(self.results['failures'][:5], 1):
                print(f"\n{i}. {failure['name']}")
                print(f"   {failure['message'][:200]}...")
            
            if len(self.results['failures']) > 5:
                print(f"\n... and {len(self.results['failures']) - 5} more failures")
        
        print("="*60)
        
        # Save results
        with open("test_summary.json", 'w') as f:
            json.dump(self.results, f, indent=2)
        print("\nDetailed results saved to: test_summary.json")

def main():
    parser = argparse.ArgumentParser(description="Run tests for code review")
    parser.add_argument("code_dir", help="Directory containing tests")
    parser.add_argument("--framework", required=True, 
                       choices=["pytest", "unittest", "jest", "junit"],
                       help="Test framework to use")
    
    args = parser.parse_args()
    
    if not Path(args.code_dir).exists():
        print(f"Error: Directory {args.code_dir} does not exist")
        sys.exit(1)
    
    runner = TestRunner(args.code_dir, args.framework)
    success = runner.run()
    runner.print_summary()
    
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
